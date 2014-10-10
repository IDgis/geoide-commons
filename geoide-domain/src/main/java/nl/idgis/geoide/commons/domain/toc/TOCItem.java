package nl.idgis.geoide.commons.domain.toc;

import java.util.List;

import scala.Symbol;
import nl.idgis.geoide.commons.domain.traits.Traits;

public class TOCItem {
	 private List<Traits<TOCItem>> items;
	 private String label;
	 private Boolean activatable;
	 private Boolean active;
	 private Symbol symbol;
	 
	 
	 public TOCItem (String label) {
		 this.label = label;
		 this.active = false;
		 this.activatable = false;
	 }
	 
	 public void setItems (List<Traits<TOCItem>> items) {
		 this.items = items;
		
	 }
	 
	 public List<Traits<TOCItem>> getItems() {
		 return items;
	 }
	 
	 public void setLabel(String label) {
		 this.label = label;
	 }
	 
	 public String getLabel() {
		 return this.label;
	 }
	 
	 public void setActivatable (Boolean activatable) {
		 this.activatable = activatable;
	 }
	 
	 public Boolean getActivatable() {
		 return this.activatable;
	 }
	 
	 public void setActive(Boolean active) {
		 this.active = active;
	 }
	 
	 public Boolean isActive() {
		 return active;
	 }
	 
	 public void setSymbol(Symbol symbol) {
		 this.symbol = symbol;
	 }
	 
	 public Symbol getSymbol() {
		 return this.symbol;
	 }

}
