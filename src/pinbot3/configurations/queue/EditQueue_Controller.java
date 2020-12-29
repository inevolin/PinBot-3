/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinbot3.configurations.queue;

import common.Http;
import common.KeyValuePair;
import java.io.File;
import java.io.IOException;
import static java.lang.Math.ceil;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Pagination;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import model.Account;
import model.configurations.AConfiguration;
import model.configurations.PinConfiguration;
import model.configurations.RepinConfiguration;
import model.configurations.queries.AQuery;
import model.pinterestobjects.Board;
import model.pinterestobjects.Pin;
import model.pinterestobjects.PinterestObject;
import model.pinterestobjects.PinterestObject.PinterestObjectResources;
import pinbot3.views.helpers.MySpinner;
import pinbot3.views.helpers.QueueItem;
import rss.Feed;
import rss.FeedMessage;
import rss.RSSFeedParser;
import scraping.QueueHelper;

public class EditQueue_Controller {

    @FXML
    private ComboBox cboBoards;
    @FXML
    private MySpinner<Integer> txtScrape;
    @FXML
    private Button btnScrape, btnImport, btnClear, btnSave, btnScrapeRSS;
    @FXML
    private AnchorPane panePagination, root;
    @FXML
    private Text txtStatus;
    @FXML
    private HBox hboxStatus;
    @FXML
    private AnchorPane anchorManual;

    private TilePane grpQueue;
    private Pagination pagination;
    private Scene scene;
    private Stage stage;

    private boolean saved = false;

    private final int MAX_PER_PAGE = 8;
    private final int HEIGHT_TILE = 250, WIDTH_TILE = 150;
    private final int GAP = 10;
    private final int COLUMNS = 4;

    private Account account;
    private AConfiguration config;
    private LinkedHashSet<Pin> queue;
    private Set<KeyValuePair> boards;
    private boolean isPinConfig;

    //Necessary for showImage
    private final int SIZE_IMG = 500;
    private ImageView img;
    private Stage stageImg;

    public void Init(Account acc, AConfiguration config, Stage stage, Scene scene) {
        isPinConfig = config instanceof PinConfiguration;
        if (isPinConfig) {
            queue = copyFromConfig(((PinConfiguration) config).getQueue());
        } else {
            queue = copyFromConfig(((RepinConfiguration) config).getQueue());
        }
        this.config = config;
        this.account = acc;
        this.stage = stage;
        //stage.setResizable(false);
        this.scene = scene;

        initMapBoards();
        initNodes();
        resetPagination();
        setEventHandlers();
        initImgView();
        pinbot3.PinBot3.dalMgr.Save(account);
    }

    private void initNodes() {
        try {
            root.setMinSize(COLUMNS * (WIDTH_TILE + GAP) + GAP + 20, (MAX_PER_PAGE / COLUMNS) * (HEIGHT_TILE + GAP) + GAP + 180);

            anchorManual.setStyle("-fx-border-width: 1;"
                    + "-fx-border-color: #D0D0D0;"
                    + "-fx-border-radius: 5px;");
            anchorManual.setMinWidth(COLUMNS * (WIDTH_TILE + GAP) + GAP + 5);

            hboxStatus.setStyle("-fx-background-color: #E8E8E8;"
                    + "-fx-border-width: 1 0 0 0;"
                    + "-fx-border-color: #D0D0D0;");
            txtStatus.setText("");
            txtStatus.setFill(Color.BLACK);

            cboBoards.getItems().clear();
            cboBoards.getItems().add("Scrape for random board");
            cboBoards.getItems().addAll(boards);

            cboBoards.getSelectionModel().selectFirst();

            txtScrape.setValue(config.getScrapeAmount() == null ? "10" : config.getScrapeAmount().toString());
            txtScrape.setMin(1);

            //Pin: because mapping is optional
            boolean disable = true;
            if (config.getQueries() != null) {
                for (AQuery query : config.getQueries()) {
                    if (query.getBoardMapped() != null) {
                        disable = false;
                        break;
                    }
                }
            }
            txtScrape.setDisable(disable);
            cboBoards.setDisable(disable);
            btnScrape.setDisable(disable);

            panePagination.setMinSize(COLUMNS * (WIDTH_TILE + GAP) + GAP + 5, (MAX_PER_PAGE / COLUMNS) * (HEIGHT_TILE + GAP) + GAP + 45);
            panePagination.setMaxSize(COLUMNS * (WIDTH_TILE + GAP) + GAP + 5, (MAX_PER_PAGE / COLUMNS) * (HEIGHT_TILE + GAP) + GAP + 45);

            btnImport.setVisible(config instanceof PinConfiguration);
            btnImport.setDisable(!(config instanceof PinConfiguration));
            btnScrapeRSS.setVisible(config instanceof PinConfiguration);
            btnScrapeRSS.setDisable(!(config instanceof PinConfiguration));

            config.setScrapeAmount(txtScrape.getValue());

        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
            
        }
    }

    //Might cause problems if different boards have the same name
    private void initMapBoards() {
        boards = new HashSet<>();
        if (config.getQueries() != null) {
            for (AQuery query : config.getQueries()) {
                Board board = (Board) query.getBoardMapped();
                KeyValuePair kvp = new KeyValuePair(board.getName(), board);
                boards.add(kvp);
            }
        }
    }

    private void initImgView() {
        img = new ImageView();
        img.setFitHeight(SIZE_IMG);
        img.setFitWidth(SIZE_IMG);
        img.setPreserveRatio(true);
        img.setOnMouseExited((MouseEvent event) -> {
            hideImage(event);
        });
        AnchorPane rootImg = new AnchorPane();
        rootImg.getChildren().add(img);

        Scene sceneImg = new Scene(rootImg);

        stageImg = new Stage();
        stageImg.setScene(sceneImg);
        stageImg.initStyle(StageStyle.UNDECORATED);
        stageImg.setAlwaysOnTop(true);
        stageImg.setX(0);
        stageImg.setY(0);
        stageImg.hide();
    }

    private void resetPagination() {
        pagination = new Pagination(pageCount());
        pagination.getStyleClass().add(Pagination.STYLE_CLASS_BULLET);
        pagination.setMaxPageIndicatorCount(5);
        pagination.setPageFactory((Integer pageIndex) -> {
            if (grpQueue != null) {
                savePins_Temp();
            }
            pagination.setPageCount(pageCount());
            return createPage(pageIndex);
        });

        panePagination.getChildren().clear();
        AnchorPane.setBottomAnchor(pagination, 0.0);
        AnchorPane.setLeftAnchor(pagination, 0.0);
        AnchorPane.setRightAnchor(pagination, 0.0);
        AnchorPane.setTopAnchor(pagination, 0.0);
        panePagination.getChildren().add(pagination);
    }

    //Deep copy only necessary for the Pin
    private LinkedHashSet<Pin> copyFromConfig(Set<Pin> queue) {
        LinkedHashSet<Pin> newQueue = new LinkedHashSet<>();
        for (Pin pin : queue) {
            newQueue.add(pin.copy(pin.getMappedQuery() == null ? null : pin.getMappedQuery().copy(null)));
        }
        return newQueue;
    }

    private Set<Pin> copyToConfig(Set<Pin> queue) {
        Set<Pin> newQueue = new HashSet<>();
        for (Pin pin : queue) {
            newQueue.add(pin.copy(pin.getMappedQuery() == null ? null : pin.getMappedQuery().copy(null)));
        }
        return newQueue;
    }

    private int pageCount() {
        if (config == null) {
            return 1;
        }
        int c = queue.size();
        if (c == 0) {
            return 1;
        } else {
            return (int) ceil((double) c / MAX_PER_PAGE);
        }
    }

    private TilePane createPage(int pageIndex) {
        if (pageIndex >= pagination.getPageCount()) {
            pageIndex = pagination.getPageCount() - 1;
        }

        grpQueue = new TilePane(Orientation.HORIZONTAL);
        grpQueue.setStyle("-fx-border-color: #D0D0D0;"
                + "-fx-border-radius: 5px;");
        grpQueue.setPadding(new Insets(GAP, GAP, GAP, GAP));
        grpQueue.setPrefColumns(COLUMNS);
        grpQueue.setTileAlignment(Pos.CENTER);
        grpQueue.setPrefTileHeight(HEIGHT_TILE);
        grpQueue.setPrefTileWidth(WIDTH_TILE);
        grpQueue.setHgap(GAP);
        grpQueue.setVgap(GAP);
        grpQueue.setMinSize(COLUMNS * (WIDTH_TILE + GAP) + GAP, (MAX_PER_PAGE / COLUMNS) * (HEIGHT_TILE + GAP) + GAP);

        if (!queue.isEmpty()) {
            createQueueItems(queue, pageIndex);
        }

        return grpQueue;
    }

    private void createQueueItems(Set<Pin> queue, int pageIndex) {
        try {
            int l = queue.size();
            Pin[] pins = new Pin[l];
            l--;
            Iterator<Pin> iterator = queue.iterator();
            while (iterator.hasNext()) {
                pins[l] = iterator.next();
                l--;
            }
            for (int i = 0; i < MAX_PER_PAGE; i++) {
                int index = pageIndex * MAX_PER_PAGE + i;
                if (index >= pins.length) {
                    break;
                }

                Board board = account.findBoard(pins[index].getDestinationBoardId());//pins[index].getMappedQuery().getBoardMapped();
                if (board == null) {
                    board = (Board) ((KeyValuePair) boards.toArray()[0]).getValue(); //take random one
                    pins[index].setDestinationBoardId(board.getBoardId());
                }
                QueueItem itm = new QueueItem(account, new KeyValuePair(board.getName(), board), pins[index], this);
                boolean addItem = true;
                for (Node n : grpQueue.getChildren()) {
                    if (((QueueItem) n).getUID().equals(itm.getUID())) {
                        addItem = false;
                    }
                }

                if (addItem) {
                    grpQueue.getChildren().add(itm);
                }
            }
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
            
        }
    }

    private void setMessage(String msg, String color) {
        txtStatus.setText(msg);
        switch (color) {
            case "red":
                txtStatus.setFill(Color.RED);
                break;

            case "orange":
                txtStatus.setFill(Color.ORANGE);
                break;

            case "green":
                txtStatus.setFill(Color.GREEN);
                break;
        }
    }

    //Doesn't make a persistent save!
    //To save in a persistent way use savePins_Persistent().
    private boolean savePins_Temp() {
        if (queue.isEmpty()) {
            return true;
        }

        try {
            for (Node n : grpQueue.getChildren()) {
                if (!(n instanceof QueueItem)) {
                    continue;
                }

                QueueItem q = (QueueItem) n;
                if (q.isDeleted()) {
                    queue.remove((Pin) q.getPinterestObject());
                } else {
                    Pin p = (Pin) q.getPinterestObject();
                    if (queue.contains(p)) {
                        queue.remove(p);
                    }
                    queue.add(p);
                }
            }
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
            
            return false;
        }
        return true;
    }

    private boolean savePins_Persistent() {
        boolean saveSuccessful = savePins_Temp();
        setQueueConfig(queue);

        return saveSuccessful;
    }

    private void setQueueConfig(Set<Pin> queue) {
        if (isPinConfig) {
            ((PinConfiguration) config).setQueue(copyToConfig(queue));
        } else {
            ((RepinConfiguration) config).setQueue(copyToConfig(queue));
        }
    }

    private void setEventHandlers() {
        txtScrape.valueProperty().addListener((ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) -> {
            if (newValue < txtScrape.getMin()) {
                txtScrape.setValue(Integer.toString(txtScrape.getMin()));
            }
        });

        btnClear.setOnAction((ActionEvent event) -> {
            try {
                if (common.Dialogs.OkCancelDialog( "Are you sure?", "Clear queue") !=  ButtonType.OK) {
                    return;
                }

                grpQueue.getChildren().clear();
                queue = new LinkedHashSet<>();
                setQueueConfig(new HashSet<>());
                saved = false;
                resetPagination();
            } catch (Exception ex) {
                common.ExceptionHandler.reportException(ex);
                
            }
        });

        btnSave.setOnAction((ActionEvent event) -> {
            if (savePins_Persistent()) {
                pinbot3.PinBot3.dalMgr.Save(account);
                saved = true;
                setMessage("Saved!", "green");
            } else {
                setMessage("Something went wrong saving.", "red");
            }
        });

        btnImport.setOnAction((ActionEvent event) -> {
            final EventHandler<MouseEvent> eventConsumer = (MouseEvent ev) -> {
                ev.consume();
            };
            scene.addEventFilter(MouseEvent.ANY, eventConsumer);
            BoardSelector_Controller bs = openBoardSelector(false);
            if (bs != null && bs.isOk()) {
                FileChooser fc = new FileChooser();
                fc.setTitle("Import image");
                fc.getExtensionFilters().add(new ExtensionFilter("Image files (*.jpg, *.jpeg, *.bmp, *.gif, *.png)", "*.jpg", "*.jpeg", "*.bmp", "*.gif", "*.png"));
                List<File> images = fc.showOpenMultipleDialog(stage);

                if (images != null) {
                    for (File fileImg : images) {
                        try {
                            Pin p = new Pin(PinterestObjectResources.External, null);
                            p.setHashUrl(fileImg.toURI().toURL().toString());
                            if (isPinConfig) {
                                String url = QueueHelper.getRandomSourceUrl((PinConfiguration) config);
                                if (url != null && !"".equals(url)) {
                                    p.setSourceUrl(url);
                                }
                            }
                            //Necessary for selectedBoard
                            AQuery externalQ = new AQuery("import", bs.getSelectedBoard(), PinterestObjectResources.External);
                            p.setDestinationBoardId(bs.getSelectedBoard().getBoardId());

                            queue.add(p);
                        } catch (MalformedURLException ex) {
                            common.ExceptionHandler.reportException(ex);
                            
                        }
                    }
                    resetPagination();
                }
            }
            scene.removeEventFilter(MouseEvent.ANY, eventConsumer);
        });

        btnScrape.setOnAction((ActionEvent event) -> {
            Set<Pin> inQueue = queue;
            Board selectedBoard;
            if (cboBoards.getSelectionModel().getSelectedIndex() == 0) {
                selectedBoard = null;
            } else {
                selectedBoard = (Board) ((KeyValuePair) cboBoards.getSelectionModel().getSelectedItem()).getValue();
            }

            config.setScrapeAmount(txtScrape.getValue());

            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                setMessage("Scraping...", "orange");
                scene.setCursor(Cursor.WAIT);
                switchDisabling(true);
                latch.countDown();
            }
            );

            //log
            new Thread() {

                @Override
                public void run() {
                    try {
                        latch.await();
                    } catch (InterruptedException ex) {
                        //ignore
                    }
                    Random r = new Random();
                    int scrapeAmount;

                    if (account.getStatus() != Account.STATUS.LOGGEDIN) {
                        try {
                            pinbot3.PinBot3.accountMgr.Login(account);
                        } catch (Exception ex) {
                            common.ExceptionHandler.reportException(ex);
                            try {
                                
                                
                                Task updateGUI = new Task() {
                                    @Override
                                    protected Object call() throws Exception {
                                        resetPagination();
                                        scene.setCursor(Cursor.DEFAULT);
                                        switchDisabling(false);
                                        setMessage("Login failed, try again?", "red");
                                        return null;
                                    }
                                };
                                Platform.runLater(updateGUI);
                                updateGUI.get();
                                
                                return;
                            } catch (InterruptedException _ex) {
                                //ignore
                            } catch (ExecutionException _ex) {
                                common.ExceptionHandler.reportException(_ex);                                
                            }
                        }
                    }

                    if (isPinConfig) {
                        PinConfiguration pinConfig = (PinConfiguration) config;
                        scrapeAmount = pinConfig.getScrapeAmount();
                    } else {
                        RepinConfiguration repinConfig = (RepinConfiguration) config;
                        scrapeAmount = repinConfig.getScrapeAmount();
                    }

                    if (selectedBoard == null) {
                        scrapeBoards(inQueue, scrapeAmount);
                    } else {
                        scrapeSelectedBoard(inQueue, scrapeAmount, selectedBoard);
                    }

                    if (isPinConfig) {
                        addToQueue(((PinConfiguration) config).getQueue());
                    } else {
                        addToQueue(((RepinConfiguration) config).getQueue());
                    }
                    saved = false;
                    try {
                        Task updateGUI = new Task() {
                            @Override
                            protected Object call() throws Exception {
                                resetPagination();
                                scene.setCursor(Cursor.DEFAULT);
                                switchDisabling(false);
                                setMessage("Scraping finished.", "green");
                                return null;
                            }
                        };
                        Platform.runLater(updateGUI);
                        updateGUI.get();
                    } catch (InterruptedException | ExecutionException ex) {
                        
                    }
                }
            }.start();

        });

        btnScrapeRSS.setOnAction((ActionEvent event) -> {
            final EventHandler<MouseEvent> eventConsumer = (MouseEvent ev) -> {
                ev.consume();
            };
            scene.addEventFilter(MouseEvent.ANY, eventConsumer);

            BoardSelector_Controller bs = openBoardSelector(true);
            if (bs != null && bs.isOk()) {
                RSSFeedParser parser = new RSSFeedParser(bs.getURL());
                Feed feed = parser.readFeed();

                for (FeedMessage message : feed.getMessages()) {
                    String sourceURL = message.getLink();
                    List<String> ls = getImagesFromHtmlString(message.getDescription());
                    
                    Pattern patternFirstPElement = Pattern.compile(".*?<p>(.+?)</p>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE & Pattern.MULTILINE);
                    Matcher mdesc = patternFirstPElement.matcher(message.getDescription());
                    
                    String desc_first_P_element = Http.unescape(mdesc.find() ? mdesc.group(1) : "");
                    desc_first_P_element = desc_first_P_element.replaceAll("\\<.*?\\>", "").trim();
                    for (String imgURL : ls) {
                        Pin p = new Pin(PinterestObjectResources.External, null);
                        p.setHashUrl(imgURL);
                        p.setSourceUrl(sourceURL);
                        //Necessary for selectedBoard
                        p.setDescription(desc_first_P_element);
                        
                        AQuery externalQ = new AQuery("URLscrape", bs.getSelectedBoard(), PinterestObjectResources.External);
                        p.setDestinationBoardId(bs.getSelectedBoard().getBoardId());

                        queue.add(p);
                    }
                }

                resetPagination();
            }
            scene.removeEventFilter(MouseEvent.ANY, eventConsumer);
        });

        stage.setOnCloseRequest((WindowEvent event) -> {
            if (!saved && !grpQueue.getChildren().isEmpty()) {
                ButtonType option = common.Dialogs.OkCancelDialog( "Would you like to save before closing?", "Save");
                
                if (option == ButtonType.OK) {
                    if (savePins_Persistent()) {
                        pinbot3.PinBot3.dalMgr.Save(account);
                    } else {
                        setMessage("Something went wrong saving.", "red");
                        event.consume();
                    }
                } else {
                    event.consume();
                }
            }
        });

    }

    private BoardSelector_Controller openBoardSelector(boolean showURL) {
        try {
            FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource("/pinbot3/configurations/queue/views/BoardSelector.fxml"));
            Parent parent = (Parent) fxmlloader.load();
            BoardSelector_Controller ctrl = fxmlloader.getController();
            Stage stageSelector = new Stage();
            ctrl.Init(account, stageSelector, showURL);
            stageSelector.getIcons().add(new Image("/pinbot3/imgs/logo.png"));
            stageSelector.setTitle(showURL ? "Scrape URL..." : "Import...");
            stageSelector.initModality(Modality.NONE);
            stageSelector.initOwner(btnImport.getScene().getWindow());
            Scene sceneSelector = new Scene(parent);
            stageSelector.setScene(sceneSelector);
            stageSelector.showAndWait();
            return ctrl;
        } catch (IOException ex) {
            common.ExceptionHandler.reportException(ex);
            
        }
        return null;
    }

    public ArrayList<String> getImagesFromHtmlString(String str) {
        ArrayList<String> arr_images = new ArrayList<>();
        Pattern pattern = Pattern.compile("(https?://\\s*\\S+\\.(?:jpg|JPG|jpeg|JPEG|png|PNG|gif|GIF|bmp|BMP))");
        Matcher m = pattern.matcher(str);
        while (m.find()) {
            arr_images.add(m.group());
        }
        return arr_images;
    }

    private void scrapeBoards(Set<Pin> inQueue, int scrapeAmount) {
        boolean prematureFinish = false;
        int scrapeCounter = 0;
        Set<Pin> configQueue;
        if (isPinConfig) {
            configQueue = ((PinConfiguration) config).getQueue();
        } else {
            configQueue = ((RepinConfiguration) config).getQueue();
        }

        int attempts = 0;

        while (!prematureFinish && scrapeCounter < scrapeAmount) {
            try {
                Map<PinterestObject, Board> tmp = pinbot3.PinBot3.scrapeMgr.Scrape(config, account, null);
                Set<Pin> scraped = null;
                if (tmp == null || (scraped = (Set) tmp.keySet()) == null) {
                    prematureFinish = true;
                    break;
                }

                int prevCounter = scrapeCounter;

                for (Pin pin : scraped) {
                    if (!inQueue.contains(pin) && account.getDuplicate(config, pin) == null) {
                        configQueue.add(pin);
                        scrapeCounter++;
                    }
                    if (scrapeCounter >= scrapeAmount) {
                        break;
                    }
                }
                tmp.clear();

                if (scrapeCounter == prevCounter) {
                    attempts++;
                    if (attempts >= 5) {
                        prematureFinish = true;
                    }
                }
            } catch (InterruptedException | ExecutionException ex) {
                common.ExceptionHandler.reportException(ex);
                
                prematureFinish = true;
            }
        }

    }

    private void scrapeSelectedBoard(Set<Pin> inQueue, int scrapeAmount, Board board) {
        List<AQuery> queries = new ArrayList<>();
        AQuery selectedQuery = null;
        for (AQuery q : config.getQueries()) {
            queries.add(q);
            if (q.getBoardMapped().equals(board)) {
                selectedQuery = q;
            }
        }

        if (selectedQuery == null) {
            return;
        }

        List<AQuery> listQuery = new ArrayList<>();
        listQuery.add(selectedQuery);
        config.setQueries(listQuery);

        scrapeBoards(inQueue, scrapeAmount);

        config.setQueries(queries);
    }

    //Deep copy only necessary for the Pin
    private void addToQueue(Set<Pin> q) {
        for (Pin pin : q) {
            if (!queue.contains(pin)) {
                if (account.getDuplicate(config, pin) != null) {
                    continue; //dupChecker
                }
                queue.add(pin);
            }
        }
    }

    private void switchDisabling(boolean disable) {
        grpQueue.setDisable(disable);
        cboBoards.setDisable(disable);
        txtScrape.setDisable(disable);
        btnClear.setDisable(disable);
        btnSave.setDisable(disable);
        btnScrape.setDisable(disable);
        btnScrapeRSS.setDisable(disable);
        btnImport.setDisable(disable);
        pagination.setDisable(disable);
    }

    public void showImage(Image image) {
        img.setImage(image);
        stageImg.show();
    }

    public void hideImage(MouseEvent event) {
        Bounds bounds = img.localToScreen((img.getBoundsInLocal()));
        if (bounds.getMaxX() < event.getScreenX() || bounds.getMaxY() < event.getScreenY()) {
            img.setImage(null);
            stageImg.hide();
        }
    }

}
