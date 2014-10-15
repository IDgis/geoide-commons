package nl.idgis.geoide.commons.domain.toc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
	private final boolean expandable;
	private final boolean expanded;
	private final Symbol symbol;
	 
	 
	 private TOCItem (
			 List<Traits<TOCItem>> items, 
			 String label, 
			 boolean activatable, 
			 boolean active,
			 boolean expandable,
			 boolean expanded,
			 Symbol symbol) {
		 this.items = items;
		 this.label = label;
		 this.activatable = activatable;
		 this.active = active;
		 this.expandable = expandable;
		 this.expanded = expanded;
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

	 
	 public boolean isExpandable() {
		return expandable;
	}

	public boolean isExpanded() {
		return expanded;
	}
	
	public static Builder builder () {
		return new Builder ();
	}

	public final static class Builder {
		private List<Traits<TOCItem>> items = new ArrayList<Traits<TOCItem>> ();
		private String label = null;
		private boolean activatable = false;
		private boolean active = false;
		private boolean expandable = false;
		private boolean expanded = false;
		private Symbol symbol = null;
		
		public TOCItem build () {
			return new TOCItem (items, label, activatable, active, expandable, expanded, symbol);
		}
		
		public List<Traits<TOCItem>> getItems() {
			return items;
		}

		public Builder addItem (final Traits<TOCItem> item) {
			this.items.add (item);
			return this;
		}
		
		public Builder setItems (final Collection<Traits<TOCItem>> items) {
			this.items = new ArrayList<Traits<TOCItem>> (items == null ? Collections.<Traits<TOCItem>>emptyList () : items);
			return this;
		}
		
		public String getLabel() {
			return label;
		}
		
		public Builder setLabel(String label) {
			this.label = label;
			return this;
		}
		
		public boolean isActivatable() {
			return activatable;
		}
		
		public Builder setActivatable(boolean activatable) {
			this.activatable = activatable;
			return this;
		}
		
		public boolean isActive() {
			return active;
		}
		
		public Builder setActive(boolean active) {
			this.active = active;
			return this;
		}
		
		public boolean isExpandable() {
			return expandable;
		}
		
		public Builder setExpandable(boolean expandable) {
			this.expandable = expandable;
			return this;
		}
		
		public boolean isExpanded() {
			return expanded;
		}
		
		public Builder setExpanded(boolean expanded) {
			this.expanded = expanded;
			return this;
		}
		
		public Symbol getSymbol() {
			return symbol;
		}
		
		public Builder setSymbol(Symbol symbol) {
			this.symbol = symbol;
			return this;
		}
	 }
}
