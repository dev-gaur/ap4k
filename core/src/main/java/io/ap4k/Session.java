/**
 * Copyright (C) 2018 Ioannis Canellos 
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **/
package io.ap4k;

import io.ap4k.config.Configuration;
import io.ap4k.config.ConfigurationSupplier;
import io.ap4k.config.KubernetesConfig;
import io.fabric8.kubernetes.api.model.KubernetesList;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * The object that holds the state used by all processors.
 * When the state is closed, the session returns multiple {@link KubernetesList} that are created using the following rules:
 *
 * 1. For each named group created by processors create a list will all items assigned explicitly to the group.
 * 2. Items added to no particular group, are appended to all groups.
 * 3. Visitors are applied to each group.
 *
 */
public class Session {

  private static Session INASTANCE;

  private final AtomicBoolean closed = new AtomicBoolean();
  private final Set<Generator> generators = new HashSet<>();
  private final Configurations configurations = new Configurations();
  private final Resources resources = new Resources();

  /**
   * Creates or resues a single instance of Session.
   * @return  The Session.
   */
  public static Session getSession() {
    if (INASTANCE != null) {
      return INASTANCE;
    }
    synchronized (Session.class) {
      if (INASTANCE == null) {
        INASTANCE = new Session();
      }
    }
    return INASTANCE;
  }

  public Configurations configurations() {
    return configurations;
  }

  public Resources resources() {
    return resources;
  }

  public Set<Generator> generators() {
    return generators;
  }

  public void onClose(Consumer<Map<String, KubernetesList>> consumer) {
    if (closed.compareAndSet(false, true)) {
      consumer.accept(close());
    }
  }

  /**
   * Close the session an get all resource groups.
   * @return A map of {@link KubernetesList} by group name.
   */
  public Map<String, KubernetesList> close() {
    this.closed.set(true);
    configurations.stream().forEach(c -> generators
        .stream().filter(g -> g.getType().isAssignableFrom(c.getClass())).forEach(g->g.generate(c)));
    return resources.generate();
  }
}