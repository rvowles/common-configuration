package com.bluetrainsoftware.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
public class PreStartRefresher implements PreStartRepository {
  private static final Logger log = LoggerFactory.getLogger(PreStartRefresher.class);
  private final List<PreStartCallback> beans = new ArrayList<>();

  private class PreStartCallback {
    private final Object object;
    private final Method method;

    public PreStartCallback(Object object, Method method) {
      this.object = object;
      this.method = method;
    }

    public Object getObject() {
      return object;
    }

    public Method getMethod() {
      return method;
    }
  }

  public void registerPreStart(Object o, Method m) {
    beans.add(new PreStartCallback(o, m));
  }

  public void start() {
    beans.forEach(psc -> {
      try {
        psc.getMethod().invoke(psc.getObject());
      } catch (IllegalAccessException|InvocationTargetException e) {
        log.info("Failed to invoke method `{}` on `{}`", psc.getMethod().getName(), psc.getObject().getClass().getName());
      }
    });

    beans.clear(); // don't let this set happen again
  }
}
