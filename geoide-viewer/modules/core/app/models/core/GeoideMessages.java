package models.core;

import java.lang.reflect.Proxy;
import java.util.Objects;

import javax.inject.Singleton;

import play.i18n.Messages;

@Singleton
public class GeoideMessages {

	public <T> T of (final Class<T> cls, final Messages messages) {
		Objects.requireNonNull (cls, "cls cannot be null");
		Objects.requireNonNull (messages, "messages cannot be null");

		final String packageName = cls.getPackage().getName ();
		final int offset = packageName.lastIndexOf ('.');
		
		final String baseName =
				(offset >= 0 ? packageName.substring (offset + 1) : packageName)
				+ "."
				+ cls.getSimpleName ().substring (0, 1).toLowerCase ()
				+ cls.getSimpleName ().substring (1);
		
		@SuppressWarnings("unchecked")
		final T result = (T) Proxy.newProxyInstance (
				cls.getClassLoader (), 
				new Class[] { cls }, 
				(proxy, method, args) -> args == null 
					? messages.at(baseName + "." + method.getName ())
					: messages.at (baseName + "." + method.getName (), args)
			);
		
		return result;
	}
}
