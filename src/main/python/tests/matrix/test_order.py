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
import random

import numpy as np
from systemds.context import SystemDSContext
from systemds.matrix import Matrix

np.random.seed(7)

shape = (random.randrange(1, 25), random.randrange(1, 25))
m = np.random.rand(shape[0], shape[1])
mx = np.random.rand(1, shape[1])
my = np.random.rand(shape[0], 1)
by = random.randrange(1, np.size(m, 1)+1)

class TestOrder(unittest.TestCase):

    sds: SystemDSContext = None

    @classmethod
    def setUpClass(cls):
        cls.sds = SystemDSContext()

    @classmethod
    def tearDownClass(cls):
        cls.sds.close()

    def test_basic(self):
        o = Matrix(self.sds, m).order(by=by, decreasing=False, index_return=False).compute()
        s = m[np.argsort(m[:, by-1])]
        self.assertTrue(np.allclose(o, s))

    def test_index(self):
        o = Matrix(self.sds, m).order(by=by, decreasing=False, index_return=True).compute()
        s = np.argsort(m[:, by - 1]) + 1
        self.assertTrue(np.allclose(np.transpose(o), s))

    def test_out_of_bounds(self):
        by_max = np.size(m, 1) + 2
        with self.assertRaises(IndexError) as context:
            Matrix(self.sds, m).order(by=by_max).compute()

    def test_decreasing(self):
        o = Matrix(self.sds, m).order(by=by, decreasing=True, index_return=True).compute()
        s = np.argsort(-m[:, by - 1]) + 1
        self.assertTrue(np.allclose(np.transpose(o), s))


if __name__ == "__main__":
    unittest.main(exit=False)
