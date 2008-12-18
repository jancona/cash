/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hive.serde2.thrift_test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.exec.ByteWritable;
import org.apache.hadoop.hive.serde2.thrift.test.Complex;
import org.apache.hadoop.hive.serde2.thrift.test.IntString;
import org.apache.hadoop.hive.serde.thrift.ThriftSerDe;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.hive.serde.Constants;

public class CreateSequenceFile {

  public static void usage() {
    System.out.println("Usage: CreateSequenceFile <output_sequencefile>");
    System.exit(1);
  }

  public static void main(String[] args) throws Exception {

    // Read parameters
    int lines = 10;
    List<String> extraArgs = new ArrayList<String>(); 
    for(int ai=0; ai<args.length; ai++) {
      if (args[ai].equals("-line") && ai + 1 < args.length) {
        lines = Integer.parseInt(args[ai+1]);
        ai++;
      } else {
        extraArgs.add(args[ai]);
      }
    }
    if (extraArgs.size() != 1) {
      usage();
    }
    
    JobConf conf = new JobConf(CreateSequenceFile.class);
    ThriftSerDe serde = new ThriftSerDe();
    Properties p = new Properties();
    p.put(Constants.SERIALIZATION_CLASS, Complex.class.getName());
    // p.put(Constants.SERIALIZATION_FORMAT, null);
    p.put(Constants.SERIALIZATION_FORMAT, com.facebook.thrift.protocol.TBinaryProtocol.class.getName());
    serde.initialize(conf, p);
    
    // Open files
    SequenceFile.Writer writer = new SequenceFile.Writer(FileSystem.get(conf), conf, new Path(extraArgs.get(0)), 
        ByteWritable.class, BytesWritable.class);

    // write to file
    ByteWritable key = new ByteWritable(0);
    
    Random rand = new Random(20081215);
    
    for(int i=0; i<lines; i++) {
      
      ArrayList<Integer> alist = new ArrayList<Integer>();
      alist.add(i); alist.add(i*2); alist.add(i*3);
      ArrayList<String> slist = new ArrayList<String>();
      slist.add("" + i*10); slist.add("" + i*100); slist.add("" + i*1000);
      ArrayList<IntString> islist = new ArrayList<IntString>();
      islist.add(new IntString(i*i, ""+ i*i*i));
      HashMap<String,String> hash = new HashMap<String,String>();
      hash.put("key_" + i, "value_" + i);
      
      Complex complex = new Complex( rand.nextInt(), 
          "record_" + (new Integer(i)).toString(),
          alist,
          slist,
          islist,
          hash);

      Writable value = serde.serialize(complex);
      writer.append(key, value);
    }
    
    // Close files
    writer.close();
  }

}