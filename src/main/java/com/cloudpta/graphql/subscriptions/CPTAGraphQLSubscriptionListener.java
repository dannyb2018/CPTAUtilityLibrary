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

import com.cloudpta.utilites.exceptions.CPTAException;

public interface CPTAGraphQLSubscriptionListener
{
    public void handleNextResultSend(CPTAGraphQLSubscription<?, ?> subscription, String nextResultAsString);
    public void handleError(CPTAGraphQLSubscription<?, ?> subscription, CPTAException error);
    public void handleClose(CPTAGraphQLSubscription<?, ?> subscription);
}
