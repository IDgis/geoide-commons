package nl.idgis.geoide.commons.domain.print;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import nl.idgis.geoide.commons.domain.document.Document;

public final class PrintEvent implements Serializable {
	private static final long serialVersionUID = 395199384496460785L;

	public static enum EventType {
		PROGRESS,
		COMPLETE,
		FAILED
	}

	private final EventType eventType;
	private final Document document;
	private final Throwable exception;

	public PrintEvent () {
		this (EventType.PROGRESS, null, null);
	}
	
	public PrintEvent (final Document document) {
		this (EventType.COMPLETE, Objects.requireNonNull (document, "document cannot be null"), null);
	}
	
	public PrintEvent (final Throwable exception) {
		this (EventType.FAILED, null, Objects.requireNonNull (exception, "exception cannot be null"));
	}
	
	PrintEvent (final EventType eventType, final Document document, final Throwable exception) {
		this.eventType = Objects.requireNonNull (eventType, "eventType cannot be null");
		this.document = document;
		this.exception = exception;
	}

	public EventType getEventType () {
		return eventType;
	}

	public Optional<Document> getDocument () {
		return document == null ? Optional.empty () : Optional.of (document);
	}

	public Optional<Throwable> getException () {
		return exception == null ? Optional.empty () : Optional.of (exception);
	}
}
