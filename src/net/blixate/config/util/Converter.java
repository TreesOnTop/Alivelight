package net.blixate.config.util;

public interface Converter<F, T> {
  T convert(F paramF);
}
