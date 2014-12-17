/*
 * Copyright 2014 Matt Dean
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
import gart.Gart;

// Parse command line args into a config map
def cli = new CliBuilder(usage: "gart [options] <op> [args]")
cli.D(args:2, valueSeparator:"=", argName:"property=value", 
    "Use value for given property")
cli._(longOpt: "daemon", "Enable gart daemon")
def options = cli.parse(args)

// Run gart
def gart = new Gart(options)
gart.run()
