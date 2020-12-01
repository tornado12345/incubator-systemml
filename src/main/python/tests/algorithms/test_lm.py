# -------------------------------------------------------------
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
# -------------------------------------------------------------

import unittest

import numpy as np
from sklearn.linear_model import LinearRegression
from systemds.context import SystemDSContext
from systemds.operator.algorithm import lm
from systemds.matrix import Matrix

np.random.seed(7)

class TestLm(unittest.TestCase):

    sds: SystemDSContext = None

    @classmethod
    def setUpClass(cls):
        cls.sds = SystemDSContext()

    @classmethod
    def tearDownClass(cls):
        cls.sds.close()

    def test_lm_simple(self):
        # if the dimensions of the input is 1, then the
        X = np.random.rand(30, 1)
        Y = np.random.rand(30, 1)
        regressor = LinearRegression(fit_intercept=False)
        model = regressor.fit(X, Y).coef_

        X_sds = Matrix(self.sds, X)
        Y_sds = Matrix(self.sds, Y)

        sds_model_weights = lm(X_sds, Y_sds).compute()
        model = model.reshape(sds_model_weights.shape)

        eps = 1e-03

        self.assertTrue(
            np.allclose(sds_model_weights, model, eps),
            "All elements are not close")

    def test_lm_invalid_shape(self):
        X = Matrix(self.sds, np.random.rand(30, 0))
        Y = Matrix(self.sds, np.random.rand(0, 1))

        with self.assertRaises(ValueError) as context:
            sds_model_weights = lm(X, Y).compute()


if __name__ == "__main__":
    unittest.main(exit=False)
