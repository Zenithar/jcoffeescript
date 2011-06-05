/*
 * Copyright 2011 David Yeung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jcoffeescript.ant;

import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.types.FileSet;
import org.junit.Test;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: zenithar
 * Date: 05/06/11
 * Time: 19:44
 * To change this template use File | Settings | File Templates.
 */
public class AntTaskTest extends BuildFileTest {

    public void setUp() {
    }

    public AntTaskTest(String message) {
        super(message);
    }

    @Test
    public void testNormalExecute() throws Exception {
        AntTask at = new AntTask();
        at.setBare(true);

        FileSet fs = new FileSet();
        fs.setDir(new File(""));
        fs.setIncludes("*.coffee");
        at.addFileSet(fs);

        at.execute();
    }
}
