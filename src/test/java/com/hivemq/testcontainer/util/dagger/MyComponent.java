package com.hivemq.testcontainer.util.dagger;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import dagger.Component;

import javax.inject.Singleton;

@SuppressWarnings("NullableProblems")
@Singleton
@Component(modules = { MyModule.class })
public interface MyComponent {

    @NotNull PublishModifier providePublishModifier();

}
