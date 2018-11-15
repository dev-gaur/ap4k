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
package io.ap4k.config;

import io.ap4k.project.Project;
import io.sundr.builder.annotations.Buildable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Buildable(builderPackage = "io.fabric8.kubernetes.api.builder")
public class Configuration {

    private final Project project;
    private final Map<ConfigKey, Object> attributes;

    public Configuration(Project project, Map<ConfigKey, Object> attributes) {
        this.project = project;
        this.attributes = attributes;
    }

    public Project getProject() {
        return project;
    }

    public Map<ConfigKey, Object> getAttributes() {
        return attributes != null
                ? Collections.unmodifiableMap(attributes)
                : Collections.emptyMap();
    }

    public <T> T getAttribute(ConfigKey<T> key) {
        return (T) attributes.get(key);
    }

    public <T> T getAttributeOrDefault(ConfigKey<T> key) {
        if (attributes.containsKey(key)) {
            return (T) attributes.get(key);
        } else if (key.getDefaultValue() != null) {
            return key.getDefaultValue();
        }
        throw new IllegalStateException("No attribute named: " + key.getName() + " was found and no default value has been configured.");
    }

    public <T> T put(ConfigKey<T> key, T value) {
        return (T) attributes.put(key, value);
    }

    public <T> boolean hasAttribute(ConfigKey<T> key) {
        return attributes.containsKey(key);
    }

    public Set<Map.Entry<ConfigKey, Object>> entrySet() {
        return attributes.entrySet();
    }
}