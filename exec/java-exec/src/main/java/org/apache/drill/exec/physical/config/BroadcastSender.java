/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.drill.exec.physical.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import org.apache.drill.exec.physical.OperatorCost;
import org.apache.drill.exec.physical.base.AbstractSender;
import org.apache.drill.exec.physical.base.PhysicalOperator;
import org.apache.drill.exec.physical.base.PhysicalVisitor;
import org.apache.drill.exec.proto.UserBitShared.CoreOperatorType;

import java.util.List;

import static org.apache.drill.exec.proto.CoordinationProtos.DrillbitEndpoint;

@JsonTypeName("broadcast-sender")
public class BroadcastSender extends AbstractSender {
  static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(BroadcastSender.class);
  private final List<DrillbitEndpoint> destinations;

  @JsonCreator
  public BroadcastSender(@JsonProperty("receiver-major-fragment") int oppositeMajorFragmentId,
                         @JsonProperty("child") PhysicalOperator child,
                         @JsonProperty("destinations") List<DrillbitEndpoint> destinations) {
    super(oppositeMajorFragmentId, child);
    this.destinations = destinations;
  }

  @Override
  public OperatorCost getCost() {
    return new OperatorCost(child.getSize().getAggSize() * destinations.size(),
                            0, 1000, child.getSize().getRecordCount());
  }

  @Override
  protected PhysicalOperator getNewWithChild(PhysicalOperator child) {
    return new BroadcastSender(oppositeMajorFragmentId, child, destinations);
  }

  @Override
  public List<DrillbitEndpoint> getDestinations() {
    return destinations;
  }

  @Override
  public <T, X, E extends Throwable> T accept(PhysicalVisitor<T, X, E> physicalVisitor, X value) throws E {
    return physicalVisitor.visitBroadcastSender(this, value);
  }

  @Override
  public int getOperatorType() {
    return CoreOperatorType.BROADCAST_SENDER_VALUE;
  }


}
