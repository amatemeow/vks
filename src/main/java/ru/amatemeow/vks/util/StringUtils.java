package ru.amatemeow.vks.util;

import java.util.Collection;

public final class StringUtils {

  public static boolean containsAny(String findIn, Collection<String> searchList, boolean caseSensitive) {
    String source = caseSensitive ? findIn : findIn.toLowerCase();
    return !searchList.stream().filter(source::contains).toList().isEmpty();
  }
}
