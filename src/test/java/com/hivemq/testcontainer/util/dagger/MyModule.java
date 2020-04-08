package com.hivemq.testcontainer.util.dagger;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import dagger.Module;
import dagger.Provides;

import javax.inject.Inject;

@Module
public class MyModule {

    @Provides
    @NotNull PublishModifier providePublishModifier() {
        return new PublishModifier();
    }

}
