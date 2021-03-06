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
package io.ap4k.config.visitors;

import io.ap4k.config.KubernetesConfigFluent;
import io.ap4k.config.Port;
import io.ap4k.deps.kubernetes.api.builder.TypedVisitor;

import java.util.Objects;

/**
 * A visitor that adds a port to all containers.
 */
public class AddPortToConfig extends TypedVisitor<KubernetesConfigFluent> {

  private final Port port;

  public AddPortToConfig(Port port) {
    this.port = port;
  }

  @Override
  public void visit(KubernetesConfigFluent config) {
    if (!hasPort(config)) {
      config.addToPorts(port);
    }
  }

  /**
   * Check if the {@link io.ap4k.config.KubernetesConfig} already has port.
   * @param config  The port.
   * @return        True if port with same container port exists.
   */
  private boolean hasPort(KubernetesConfigFluent config) {
    for (Port p : config.getPorts()) {
      if (p.getContainerPort() == port.getContainerPort())  {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AddPortToConfig addPortToConfig = (AddPortToConfig) o;
    return Objects.equals(port, addPortToConfig.port);
  }

  @Override
  public int hashCode() {

    return Objects.hash(port);
  }
}
