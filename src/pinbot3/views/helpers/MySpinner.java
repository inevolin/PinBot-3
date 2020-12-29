/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinbot3.views.helpers;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

public class MySpinner<T> extends Spinner<Integer> {

    private final ObjectProperty<Integer> mode = new SimpleObjectProperty<>();

    public MySpinner() {
        this.getEditor().focusedProperty().addListener((s, ov, nv) -> {
            if (nv) {
                return;
            }
            commitEditorText(this);
        });

        SpinnerValueFactory<Integer> valueFactory;
        valueFactory = new SpinnerValueFactory<Integer>() {
            @Override
            public void decrement(int steps) {
                Integer delta = getValue() - steps;
                setValue(delta);
            }

            @Override
            public void increment(int steps) {
                Integer delta = getValue() + steps;
                setValue(delta);
            }

        };

        this.setValueFactory(valueFactory);
    }

    public void setValue(String nv) {
        getEditor().textProperty().set(nv == null ? "" : nv);
        commitEditorText(this);
    }
    private void commitEditorText(Spinner<Integer> spinner) {
        Integer f = Integer.valueOf(128);
        if (!spinner.isEditable()) {
            return;
        }
        String text = spinner.getEditor().getText();
        SpinnerValueFactory<Integer> valueFactory = spinner.getValueFactory();
        if (valueFactory != null) {
            Integer value = text.equals("") ? 0 : Integer.parseInt(text);
            valueFactory.setValue(value);
        }
    }

    private int amountToStepBy, initialValue, max, min;

    public int getInitialValue() {
        return initialValue;
    }

    public void setInitialValue(int initialValue) {
        this.initialValue = initialValue;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getAmountToStepBy() {
        return amountToStepBy;
    }

    public void setAmountToStepBy(int amountToStepBy) {
        this.amountToStepBy = amountToStepBy;
    }

}
