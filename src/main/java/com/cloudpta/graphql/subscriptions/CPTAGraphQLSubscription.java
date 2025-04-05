/*

Copyright 2017-2019 Advanced Products Limited, 
Copyright 2021-2022 Liquid Markets Limited, 
github.com/dannyb2018

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/
package com.cloudpta.graphql.subscriptions;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import com.cloudpta.graphql.common.CPTAGraphQLInput;
import graphql.ExecutionResult;

// We have the publisher and subscriber as the same class because we want to have the option to do keep alives which 
// reactive streams bizarrely doesnt allow for
public abstract class CPTAGraphQLSubscription<ResultType,RequestType extends CPTAGraphQLInput> implements Subscriber<ExecutionResult>
{
    public abstract void parseArguments(RequestType input);

    @Override
    public void onSubscribe(Subscription s) 
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onSubscribe'");
    }

    @Override
    public void onNext(ExecutionResult t) 
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onNext'");
    }

    @Override
    public void onError(Throwable t) 
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onError'");
    }

    @Override
    public void onComplete() 
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onComplete'");
    }

    protected String id;
}
