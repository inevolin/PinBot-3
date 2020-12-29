/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

/**
 *
 * @author UGent
 */
public class KeyValuePair<T> {

    private String key;
    private T value;

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public KeyValuePair(String key, T value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public T getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return key; //do not change! GUI
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        
        hash += key.hashCode();
        hash += (value != null ? value.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof KeyValuePair)) {
            return false;
        }
        
        KeyValuePair other = (KeyValuePair) object;

        if ((this.key == null && other.getKey() != null) || (this.key != null && !this.key.equals(other.getKey()))) {
            return false;
        } else if ((this.value == null && other.getValue() != null) || (this.value != null && !this.value.equals(other.getValue()))) {
            return false;
        }
        return true;
    }

}
