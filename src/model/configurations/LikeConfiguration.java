/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.configurations;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "Like")
public class LikeConfiguration extends AConfiguration {

    //hashCode & equals MUST be defined, or Campaign::HashSet<Configuration> won't work properly.
    

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof LikeConfiguration)) {
            return false;
        }
        LikeConfiguration other = (LikeConfiguration) object;
        if ( ((this.getId() == null && other.getId() != null) || (this.getId() != null && !this.getId().equals(other.getId())))) {
            return false;
        }
        return true;
    }
}
