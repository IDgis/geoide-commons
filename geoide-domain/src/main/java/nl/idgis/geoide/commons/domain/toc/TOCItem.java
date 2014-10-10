package nl.idgis.geoide.commons.domain.toc;

import java.io.Serializable;
import java.util.List;


import nl.idgis.geoide.commons.domain.traits.Traits;

public final class TOCItem implements Serializable {
	 /**
	 * 
	 */
	private static final long serialVersionUID = -8476704521251938563L;
	private final List<Traits<TOCItem>> items;
	private final String label;
	private final boolean activatable;
	private final boolean active;
	private final Symbol symbol;
	 
	 
	 public TOCItem (List<Traits<TOCItem>> items, String label, boolean activatable, boolean active, Symbol symbol ) {
		 this.items = items;
		 this.label = label;
		 this.activatable = activatable;
		 this.active = active;
		 this.symbol = symbol;
		 
	 }
	  
	 public List<Traits<TOCItem>> getItems() {
		 return items;
	 }
	 

	 public String getLabel() {
		 return this.label;
	 }
	 
	 public Boolean getActivatable() {
		 return this.activatable;
	 }

	 public Boolean isActive() {
		 return active;
	 }
	  
	 public Symbol getSymbol() {
		 return this.symbol;
	 }

}
