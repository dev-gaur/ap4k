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
package io.ap4k.openshift.processor;

import io.ap4k.Session;
import io.ap4k.annotation.KubernetesApplication;
import io.ap4k.config.ConfigurationSupplier;
import io.ap4k.openshift.OpenshiftGenerator;
import io.ap4k.openshift.annotation.OpenshiftApplication;
import io.ap4k.openshift.confg.OpenshiftConfigCustomAdapter;
import io.ap4k.openshift.config.OpenshiftConfig;
import io.ap4k.openshift.config.OpenshiftConfigBuilder;
import io.ap4k.processor.AbstractAnnotationProcessor;
import io.ap4k.project.Project;
import io.ap4k.project.AptProjectFactory;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.HashSet;
import java.util.Set;

@SupportedAnnotationTypes({"io.ap4k.annotation.KubernetesApplication", "io.ap4k.openshift.annotation.OpenshiftApplication"})
public class OpenshiftAnnotationProcessor extends AbstractAnnotationProcessor<OpenshiftConfig> {

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Session session = Session.getSession();
        if  (roundEnv.processingOver()) {
            session.onClose(r -> write(r));
            return true;
        }
        Set<Element> mainClasses = new HashSet<>();
        for (TypeElement typeElement : annotations) {
            for (Element mainClass : roundEnv.getElementsAnnotatedWith(typeElement)) {
                mainClasses.add(mainClass);
            }
        }

        for (Element mainClass : mainClasses) {
          session.configurations().add(configuration(mainClass));
          session.generators().add(new OpenshiftGenerator(session.resources()));
        }
        return false;
    }

  @Override
  public ConfigurationSupplier<OpenshiftConfig> configuration(Element mainClass) {
    return new ConfigurationSupplier<OpenshiftConfig>(configurationBuilder(mainClass));
  }

  /**
     * Get or newBuilder a new config for the specified {@link Element}.
     * @param mainClass     The type element of the annotated class (Main).
     * @return              A new config.
     */
    public OpenshiftConfigBuilder configurationBuilder(Element mainClass) {
        Project project = AptProjectFactory.create(processingEnv);
        OpenshiftApplication openshiftApplication = mainClass.getAnnotation(OpenshiftApplication.class);
        KubernetesApplication kubernetesApplication = mainClass.getAnnotation(KubernetesApplication.class);
        return OpenshiftConfigCustomAdapter.newBuilder(project, openshiftApplication, kubernetesApplication);
    }
}
