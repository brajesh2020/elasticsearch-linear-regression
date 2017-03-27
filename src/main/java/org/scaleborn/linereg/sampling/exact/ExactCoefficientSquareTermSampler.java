/*
 * Copyright (c) 2017 Scaleborn UG, www.scaleborn.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.scaleborn.linereg.sampling.exact;

import org.scaleborn.linereg.sampling.Sampler.CoefficientSquareTermSampler;

/**
 * Samples square term data with covariance matrix solved exactly. Doing this that
 * way result in a time complexity of O(C² * N) and a memory consumption of O(C²), where C is
 * the count of variables and N the count of observations / documents.
 * Created by mbok on 27.03.17.
 */
public class ExactCoefficientSquareTermSampler implements
    CoefficientSquareTermSampler<ExactCoefficientSquareTermSampler> {

  /**
   * TODO: Migrate to another algorithm to avoid sums of products, which can lead to numerical
   * instability as well as to arithmetic overflow.
   */
  private double[][] featuresProductSums;
  private ExactSamplingContext context;

  public ExactCoefficientSquareTermSampler(ExactSamplingContext context) {
    this.context = context;
    int featuresCount = context.getFeaturesCount();
    featuresProductSums = new double[featuresCount][];
    for (int i = 0; i < featuresCount; i++) {
      featuresProductSums[i] = new double[featuresCount];
    }
  }

  @Override
  public double[][] getCovarianceLowerTriangularMatrix() {
    int featuresCount = context.getFeaturesCount();
    long count = context.getCount();
    double[][] covMatrix = new double[featuresCount][];
    double[] averages = context.getFeaturesMean();
    double[] featureSums = context.featureSums;
    for (int i = 0; i < featuresCount; i++) {
      double avgI = averages[i];
      covMatrix[i] = new double[featuresCount];
      // Iterate until "i" due to the covariance matrix is symmetric and
      // build only the lower triangle
      for (int j = 0; j <= i; j++) {
        double avgJ = averages[j];
        covMatrix[i][j] = featuresProductSums[i][j] - avgI * featureSums[j] - avgJ * featureSums[i]
            + count * avgI * avgJ;
      }
    }
    return covMatrix;
  }

  @Override
  public void sample(final double[] featureValues, final double responseValue) {
    int featuresCount = context.getFeaturesCount();
    for (int i = 0; i < featuresCount; i++) {
      double vi = featureValues[i];
      for (int j = 0; j < featuresCount; j++) {
        featuresProductSums[i][j] += vi * featureValues[j];
      }
    }
  }

  @Override
  public void merge(final ExactCoefficientSquareTermSampler fromSample) {
    int featuresCount = context.getFeaturesCount();
    for (int i = 0; i < featuresCount; i++) {
      for (int j = 0; j < featuresCount; j++) {
        featuresProductSums[i][j] += fromSample.featuresProductSums[i][j];
      }
    }
  }
}