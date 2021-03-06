package io.ap4k.visitor;

import io.ap4k.config.PersistentVolumeClaimVolume;
import io.ap4k.deps.kubernetes.api.builder.TypedVisitor;
import io.ap4k.deps.kubernetes.api.model.PodSpecBuilder;

public class AddPvcVolume extends TypedVisitor<PodSpecBuilder> {

    private final PersistentVolumeClaimVolume volume;

    public AddPvcVolume(PersistentVolumeClaimVolume volume) {
        this.volume = volume;
    }

    @Override
    public void visit(PodSpecBuilder podSpec) {
        podSpec.addNewVolume()
                .withName(volume.getVolumeName())
                .withNewPersistentVolumeClaim()
                .withClaimName(volume.getClaimName())
                .withNewReadOnly(volume.isReadOnly())
                .endPersistentVolumeClaim();

    }
}
