package io.ap4k.visitor;

import io.ap4k.config.Probe;
import io.ap4k.utils.Strings;
import io.ap4k.deps.kubernetes.api.builder.TypedVisitor;
import io.ap4k.deps.kubernetes.api.model.ContainerBuilder;
import io.ap4k.deps.kubernetes.api.model.ExecAction;
import io.ap4k.deps.kubernetes.api.model.HTTPGetAction;
import io.ap4k.deps.kubernetes.api.model.IntOrString;
import io.ap4k.deps.kubernetes.api.model.ProbeBuilder;
import io.ap4k.deps.kubernetes.api.model.TCPSocketAction;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;

public abstract class AbstractAddProbe extends TypedVisitor<ContainerBuilder> {

    protected final Probe probe;

    public AbstractAddProbe(Probe probe) {
        this.probe = probe;
    }

    public abstract void visit(ContainerBuilder container);


    /**
     * Convert the internal {@link Probe} to the kubernetes-model {@link io.ap4k.deps.kubernetes.api.model.Probe}.
     * @param probe The inrenal probe.
     * @return      The fabirc8 probe.
     */
    protected io.ap4k.deps.kubernetes.api.model.Probe convert(Probe probe) {
       return new ProbeBuilder()
               .withExec(execAction(probe))
                .withHttpGet(httpGetAction(probe))
                .withTcpSocket(tcpSocketAction(probe))
                .withInitialDelaySeconds(probe.getInitialDelaySeconds())
                .withPeriodSeconds(probe.getPeriodSeconds())
                .withTimeoutSeconds(probe.getTimeoutSeconds())
               .build();
    }

    /**
     * Convert {@link Probe} to an {@link ExecAction}.
     * @param probe The probe.
     * @return      The exec action, or null if no exec configuration is present.
     */
    protected ExecAction execAction(Probe probe) {
        if (Strings.isNullOrEmpty(probe.getExecAction())) {
            return null;
        }
       return new ExecAction(Arrays.asList(probe.getExecAction().split(" ")));
    }

    /**
     * Convert the {@link Probe} to an {@link HTTPGetAction}.
     * @param probe     The probe.
     * @return          The http get action, or null if not http configuration is present.
     */
    protected HTTPGetAction httpGetAction(Probe probe) {
        if (Strings.isNullOrEmpty(probe.getHttpAction())) {
            return null;
        }

        try {
            URI uri = new URI(probe.getHttpAction());
            return new HTTPGetAction(uri.getHost(), Collections.emptyList(), uri.getPath(), new IntOrString(uri.getPort()), uri.getScheme());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert the {@link Probe} to an {@link TCPSocketAction}.
     * @param probe     The probe.
     * @return          The tcp socket action, or null if not tcp socket is present.
     */
    protected TCPSocketAction tcpSocketAction(Probe probe) {
       if (Strings.isNullOrEmpty(probe.getTcpSocketAction()))  {
           return null;
       }

       String[] parts = probe.getTcpSocketAction().split(":");
       if (parts.length != 2) {
           throw  new RuntimeException("Invalid format for tcp socket action! Expected: <host>:<port>. Found:"+probe.getTcpSocketAction()+".");
       }

       return new TCPSocketAction(parts[0], new IntOrString(parts[1]));
    }
}
