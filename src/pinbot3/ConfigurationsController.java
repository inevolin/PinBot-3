/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinbot3;

import common.KeyValuePair;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextInputDialog;
import javafx.scene.paint.Color;
import model.Account;
import model.Campaign;
import model.configurations.CommentConfiguration;
import model.configurations.FollowConfiguration;
import model.configurations.InviteConfiguration;
import model.configurations.LikeConfiguration;
import model.configurations.MessageConfiguration;
import model.configurations.PinConfiguration;
import model.configurations.RepinConfiguration;
import model.configurations.UnfollowConfiguration;
import pinbot3.configurations.Configuration_CommentController;
import pinbot3.configurations.Configuration_FollowController;
import pinbot3.configurations.Configuration_InviteController;
import pinbot3.configurations.Configuration_LikeController;
import pinbot3.configurations.Configuration_MessageController;
import pinbot3.configurations.Configuration_PinController;
import pinbot3.configurations.Configuration_RepinController;
import pinbot3.configurations.Configuration_UnfollowController;

public class ConfigurationsController implements Initializable {

    @FXML
    private ChoiceBox cboCampaign;
    @FXML
    private Tab cfPin, cfRepin, cfLike, cfInvite, cfFollow, cfUnfollow, cfComment, cfMessage;
    @FXML
    private Button btnAdd, btnEdit, btnDelete, btnSave;
    @FXML
    private Label lblStatus;

    private Account account;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    public void Init(Account acc) {
        this.account = acc;
        initCampagins();
        initBtns();
    }

    void initTabs() {
        initPin();
        initRepin();
        initLike();
        initInvite();
        initFollow();
        initUnfollow();
        initComment();
        initMessage();
    }

    void initBtns() {
        btnAdd.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                String s = prompt("");
                if (s != null && !s.equals("")) {
                    Campaign campaign = new Campaign();
                    campaign.setCampaignName(s);
                    account.getCampaigns().add(campaign);
                    account.enableCampaign(campaign);

                    KeyValuePair o = new KeyValuePair(s, campaign);
                    cboCampaign.getItems().add(o);
                    cboCampaign.getSelectionModel().select(o);
                    pinbot3.PinBot3.dalMgr.Save(account);
                }
            }
        });
        btnEdit.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                String s = prompt("");
                if (s != null && !s.equals("")) {
                    KeyValuePair o = (KeyValuePair) cboCampaign.getSelectionModel().getSelectedItem();
                    cboCampaign.getItems().remove(o);
                    account.getCampaigns().remove((Campaign) o.getValue());
                    ((Campaign) o.getValue()).setCampaignName(s);
                    o = new KeyValuePair(s, o.getValue());
                    cboCampaign.getItems().add(o);
                    account.getCampaigns().add((Campaign) o.getValue());
                    account.enableCampaign((Campaign) o.getValue());

                    cboCampaign.getSelectionModel().select(o);
                    pinbot3.PinBot3.dalMgr.Save(account);
                }
            }
        });
        btnDelete.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                KeyValuePair o = (KeyValuePair) cboCampaign.getSelectionModel().getSelectedItem();
                if (cboCampaign.getItems().size() > 0) {
                    cboCampaign.getItems().remove(o);
                }
                if (account.getCampaigns().size() > 0) {
                    account.getCampaigns().remove((Campaign) o.getValue());
                }
                if (account.getCampaigns().size() > 0) {
                    account.enableCampaign((Campaign) account.getCampaigns().toArray()[0]);
                    cboCampaign.getSelectionModel().selectFirst();
                } else {
                    Campaign campaign = new Campaign();
                    campaign.setCampaignName("Default");
                    account.getCampaigns().add(campaign);
                    account.enableCampaign(campaign);
                }
                pinbot3.PinBot3.dalMgr.Save(account);
                if (cboCampaign.getItems().size() <= 1) {
                    btnDelete.setDisable(true);
                } else {
                    btnDelete.setDisable(false);
                }
            }
        });
        initBtnSave();
    }

    void initBtnSave() {
        btnSave.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                saveAction();
            }
        });
    }

    void saveAction() {
        String s = validateAll();

        if (s == null) {
            pinbot3.PinBot3.dalMgr.Save(account);
            setStatus("Saved!", Color.GREEN);
        }
    }

    public String validateAll() {
        String s = null;

        s = ((Configuration_PinController) cfPin.getContent()).toCf();
        setStatus(s, Color.RED);
        if (s != null) {
            return s;
        }
        s = ((Configuration_RepinController) cfRepin.getContent()).toCf();
        setStatus(s, Color.RED);
        if (s != null) {
            return s;
        }
        s = ((Configuration_LikeController) cfLike.getContent()).toCf();
        setStatus(s, Color.RED);
        if (s != null) {
            return s;
        }
        s = ((Configuration_InviteController) cfInvite.getContent()).toCf();
        setStatus(s, Color.RED);
        if (s != null) {
            return s;
        }
        s = ((Configuration_FollowController) cfFollow.getContent()).toCf();
        setStatus(s, Color.RED);
        if (s != null) {
            return s;
        }
        s = ((Configuration_UnfollowController) cfUnfollow.getContent()).toCf();
        setStatus(s, Color.RED);
        if (s != null) {
            return s;
        }
        s = ((Configuration_CommentController) cfComment.getContent()).toCf();
        setStatus(s, Color.RED);
        if (s != null) {
            return s;
        }
        s = ((Configuration_MessageController) cfMessage.getContent()).toCf();
        setStatus(s, Color.RED);
        if (s != null) {
            return s;
        }

        setStatus("", Color.GREEN); //all good, next operation will set label if needed
        return null;
    }

    public void setStatus(String s, Color c) {
        if (s != null) {
            lblStatus.setText(s);
            lblStatus.setTextFill(c);
        }
    }

    String prompt(String s) {
        //http://code.makery.ch/blog/javafx-dialogs-official/
        TextInputDialog dialog = new TextInputDialog(s);
        dialog.setTitle("Enter name...");
        dialog.setHeaderText("");
        dialog.setContentText("name: ");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            System.out.println(result.get());
            return result.get();
        }
        return "";
    }

    void initCampagins() {        
        initCboCampaign();
        Campaign campaign = account.getSelectCampaign();
        if (campaign == null && account.getCampaigns().size() == 0) {
            campaign = new Campaign();
            campaign.setCampaignName("Default");

            account.getCampaigns().add(campaign);
            account.enableCampaign(campaign);

            pinbot3.PinBot3.dalMgr.Save(account);
        } else if (campaign == null && account.getCampaigns().size() > 0) {
            account.enableCampaign(account.getCampaigns().iterator().next());
            campaign = account.getSelectCampaign();
        }

        for (Campaign c : account.getCampaigns()) {
            KeyValuePair kv = new KeyValuePair(c.getCampaignName(), c);
            cboCampaign.getItems().add(kv); // fill combobox
            if (c == account.getSelectCampaign()) {
                cboCampaign.getSelectionModel().select(kv);
            }
        }

        if (cboCampaign.getItems().size() <= 1) {
            btnDelete.setDisable(true);
        } else {
            btnDelete.setDisable(false);
        }
        
    }
    private void initCboCampaign() {
        cboCampaign.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<KeyValuePair>() {
            @Override
            public void changed(ObservableValue<? extends KeyValuePair> observable, KeyValuePair oldValue, KeyValuePair newValue) {
                if (newValue == null) {
                    btnEdit.setDisable(true);
                    btnDelete.setDisable(true);
                    return;
                } else {
                    btnEdit.setDisable(false);
                    if (cboCampaign.getItems().size() > 1) {
                        btnDelete.setDisable(false);
                    }
                }
                for (Campaign campaign : account.getCampaigns()) {
                    if (newValue.getValue() == campaign) {
                        account.enableCampaign(campaign);
                        break;
                    }
                }
                initTabs();
                //saveAction();
            }
        });
    }

    private void initPin() {
        PinConfiguration pcf = null;
        try {
            pcf = (PinConfiguration) account.getSelectCampaign().getConfigurations().stream().filter(x -> x instanceof PinConfiguration).findFirst().get();
        } catch (NoSuchElementException ex) {
            pcf = new PinConfiguration();
            account.getSelectCampaign().getConfigurations().add(pcf);
        }
        Configuration_PinController cpc = new Configuration_PinController(account, pcf, this);
        cfPin.setContent(cpc);
    }

    private void initRepin() {
        RepinConfiguration pcf = null;
        try {
            pcf = (RepinConfiguration) account.getSelectCampaign().getConfigurations().stream().filter(x -> x instanceof RepinConfiguration).findFirst().get();
        } catch (NoSuchElementException ex) {
            pcf = new RepinConfiguration();
            account.getSelectCampaign().getConfigurations().add(pcf);
        }
        Configuration_RepinController cpc = new Configuration_RepinController(account, pcf, this);
        cfRepin.setContent(cpc);
    }

    private void initLike() {
        LikeConfiguration pcf = null;
        try {
            pcf = (LikeConfiguration) account.getSelectCampaign().getConfigurations().stream().filter(x -> x instanceof LikeConfiguration).findFirst().get();
        } catch (NoSuchElementException ex) {
            pcf = new LikeConfiguration();
            account.getSelectCampaign().getConfigurations().add(pcf);
        }
        Configuration_LikeController cpc = new Configuration_LikeController(account, pcf);
        cfLike.setContent(cpc);
    }

    void initInvite() {
        InviteConfiguration pcf = null;
        try {
            pcf = (InviteConfiguration) account.getSelectCampaign().getConfigurations().stream().filter(x -> x instanceof InviteConfiguration).findFirst().get();
        } catch (NoSuchElementException ex) {
            pcf = new InviteConfiguration();
            account.getSelectCampaign().getConfigurations().add(pcf);
        }
        Configuration_InviteController cpc = new Configuration_InviteController(account, pcf);
        cfInvite.setContent(cpc);
    }

    void initFollow() {
        FollowConfiguration pcf = null;
        try {
            pcf = (FollowConfiguration) account.getSelectCampaign().getConfigurations().stream().filter(x -> x instanceof FollowConfiguration).findFirst().get();
        } catch (NoSuchElementException ex) {
            pcf = new FollowConfiguration();
            account.getSelectCampaign().getConfigurations().add(pcf);
        }
        Configuration_FollowController cpc = new Configuration_FollowController(account, pcf);
        cfFollow.setContent(cpc);
    }

    void initUnfollow() {
        UnfollowConfiguration pcf = null;
        try {
            pcf = (UnfollowConfiguration) account.getSelectCampaign().getConfigurations().stream().filter(x -> x instanceof UnfollowConfiguration).findFirst().get();
        } catch (NoSuchElementException ex) {
            pcf = new UnfollowConfiguration();
            account.getSelectCampaign().getConfigurations().add(pcf);
        }
        Configuration_UnfollowController cpc = new Configuration_UnfollowController(account, pcf);
        cfUnfollow.setContent(cpc);
    }

    void initComment() {
        CommentConfiguration pcf = null;
        try {
            pcf = (CommentConfiguration) account.getSelectCampaign().getConfigurations().stream().filter(x -> x instanceof CommentConfiguration).findFirst().get();
        } catch (NoSuchElementException ex) {
            pcf = new CommentConfiguration();
            account.getSelectCampaign().getConfigurations().add(pcf);
        }
        Configuration_CommentController cpc = new Configuration_CommentController(account, pcf);
        cfComment.setContent(cpc);
    }

    void initMessage() {
        MessageConfiguration pcf = null;
        try {
            pcf = (MessageConfiguration) account.getSelectCampaign().getConfigurations().stream().filter(x -> x instanceof MessageConfiguration).findFirst().get();

        } catch (NoSuchElementException ex) {
            pcf = new MessageConfiguration();
            account.getSelectCampaign().getConfigurations().add(pcf);
        }
        Configuration_MessageController cpc = new Configuration_MessageController(account, pcf);
        cfMessage.setContent(cpc);
    }

}
