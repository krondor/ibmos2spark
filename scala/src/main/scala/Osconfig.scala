package com.ibm.ibmos2spark

import scala.collection.mutable.HashMap
import org.apache.spark.SparkContext


object urlbuilder{
  def swifturl(name: String, container_name: String, object_name: String): String = {
    if (name contains '_'){
      throw new IllegalArgumentException("The swift protocol does not support underscores (_) in " + name);
    }
    if ( container_name contains '_'){
        throw new IllegalArgumentException("The swift protocol does not support underscores (_) in " + container_name);
    }
   return "swift://" + container_name  + "." + name + "/" + object_name
  }

  def swifturl2d(name: String, container_name: String, object_name: String): String = {
   return "swift2d://" + container_name  + "." + name + "/" + object_name
  }
}

class softlayer(sc: SparkContext, name: String, auth_url: String, 
               username: String, password: String, region: String, public: Boolean=false){
    
    /**  sparkcontext is a SparkContext object.
      *  name is a string that identifies this configuration. You can
      *      use any string you like. This allows you to create
      *      multiple configurations to different Object Storage accounts.
      *  auth_url, username and password are string credentials for your
      *  Softlayer Object Store
      */
    if (name contains '_'){
    throw new IllegalArgumentException("The swift protocol does not support underscores (_) in " + name);
      }
    
    val hadoopConf = sc.hadoopConfiguration;
    val prefix = "fs.swift.service." + name 

    hadoopConf.set(prefix + ".auth.url",auth_url)
    hadoopConf.set(prefix + ".tenant",username)
    hadoopConf.set(prefix + ".username",username)
    hadoopConf.set(prefix + ".auth.endpoint.prefix","endpoints")
    hadoopConf.set(prefix + ".password",password)
    hadoopConf.set(prefix + ".use.get.auth","true")
    hadoopConf.setBoolean(prefix + ".location-aware",false)
    hadoopConf.setInt(prefix + ".http.port",8080)
    hadoopConf.set(prefix + ".apikey",password)
    hadoopConf.setBoolean(prefix + ".public",public)
    hadoopConf.set(prefix + ".region", region)

    
    def url(container_name: String, object_name:String) : String= {
        return(urlbuilder.swifturl(name= name, container_name,object_name))
    }
}


class softlayer2d(sc: SparkContext, name: String, auth_url: String, 
                  tenant: String, username: String, password: String, 
                  swift2d_driver: String = "com.ibm.stocator.fs.ObjectStoreFileSystem",
                  public: Boolean=false){
    
    /** sparkcontext is a SparkContext object.
      * name is a string that identifies this configuration. You can
      *    use any string you like. This allows you to create
      *    multiple configurations to different Object Storage accounts.
      * auth_url, tenant, username and password are string credentials for your
      * Softlayer Object Store
      */
    
    val hadoopConf = sc.hadoopConfiguration;
    val prefix = "fs.swift2d.service." + name 

    hadoopConf.set("fs.swift2d.impl",swift2d_driver)
    hadoopConf.set(prefix + ".auth.url",auth_url)
    hadoopConf.set(prefix + ".username", username)
    hadoopConf.set(prefix + ".tenant", tenant)
    hadoopConf.set(prefix + ".auth.endpoint.prefix","endpoints")
    hadoopConf.set(prefix + ".auth.method","swiftauth")
    hadoopConf.setInt(prefix + ".http.port",8080)
    hadoopConf.set(prefix + ".apikey",password)
    hadoopConf.setBoolean(prefix + ".public",public)
    hadoopConf.set(prefix + ".use.get.auth","true")
    hadoopConf.setBoolean(prefix + ".location-aware",false)
    hadoopConf.set(prefix + ".password",password)

    
    def url(container_name: String, object_name:String) : String= {
        return(urlbuilder.swifturl2d(name= name, container_name,object_name))
    }
}



class bluemix(sc: SparkContext, name: String, creds: HashMap[String, String]){

  /**  sparkcontext:  a SparkContext object.
   * credentials:  a dictionary with the following required keys:
   *   
   *   auth_url
   *   project_id (or projectId)
   *   user_id (or userId)
   *   password
   *   region
   * and optional key:
   *   name  #[to be deprecated] The name of the configuration.
   * name:  string that identifies this configuration. You can
   *     use any string you like. This allows you to create
   *     multiple configurations to different Object Storage accounts.
   *     This is not required at the moment, since credentials['name']
   *     is still supported.
   * When using this from a IBM Spark service instance that
   * is configured to connect to particular Bluemix object store
   * instances, the values for these credentials can be obtained
   * by clicking on the 'insert to code' link just below a data
   * source.
   */
    
    if (name contains '_'){
    throw new IllegalArgumentException("The swift protocol does not support underscores (_) in " + name);
      }
    
    def ifexist(credsin: HashMap[String, String], var1: String, var2: String): String = {
        if (credsin.keySet.exists(_ == var1)){
            return(credsin(var1))
        }else {
           return(credsin(var2))
        }
    }

    val username = ifexist(creds, "user_id","userId")
    val tenant = ifexist(creds, "project_id","projectId")
    
    val hadoopConf = sc.hadoopConfiguration;
    val prefix = "fs.swift.service." + name;

    hadoopConf.set(prefix + ".auth.url",creds("auth_url") + "/v3/auth/tokens")
    hadoopConf.set(prefix + ".tenant",tenant)
    hadoopConf.set(prefix + ".username",username)
    hadoopConf.set(prefix + ".password",creds("password"))
    hadoopConf.set(prefix + ".region",creds("region"))
    hadoopConf.setInt(prefix + ".http.port",8080)
    
    
    def url(container_name: String, object_name:String) : String= {
        return(urlbuilder.swifturl(name= name, container_name,object_name))
    }
}



class bluemix2d(sc: SparkContext, name: String, creds: HashMap[String, String],
                swift2d_driver: String = "com.ibm.stocator.fs.ObjectStoreFileSystem", 
                public: Boolean =false){
    
  /** sparkcontext:  a SparkContext object.
    * credentials:  a dictionary with the following required keys:
    *   
    *   auth_url
    *   project_id (or projectId)
    *   user_id (or userId)
    *   password
    *   region
    * and optional key:
    *   name  #[to be deprecated] The name of the configuration.
    * name:  string that identifies this configuration. You can
    *     use any string you like. This allows you to create
    *     multiple configurations to different Object Storage accounts.
    *     This is not required at the moment, since credentials['name']
    *     is still supported.
    * When using this from a IBM Spark service instance that
    * is configured to connect to particular Bluemix object store
    * instances, the values for these credentials can be obtained
    * by clicking on the 'insert to code' link just below a data
    * source.
   */
    def ifexist(credsin: HashMap[String, String], var1: String, var2: String): String = {
        if (credsin.keySet.exists(_ == var1)){
            return(credsin(var1))
        }else {
           return(credsin(var2))
        }
    }

    val username = ifexist(creds, "user_id","userId")
    val tenant = ifexist(creds, "project_id","projectId")

    
    val hadoopConf = sc.hadoopConfiguration;
    val prefix = "fs.swift2d.service." + name;

    hadoopConf.set("fs.swift2d.impl",swift2d_driver)

    hadoopConf.set(prefix + ".auth.url",creds("auth_url") + "/v3/auth/tokens")
    hadoopConf.set(prefix + ".auth.endpoint.prefix","endpoints")
    hadoopConf.set(prefix + ".auth.method","keystoneV3")
    hadoopConf.set(prefix + ".tenant",tenant)
    hadoopConf.set(prefix + ".username",username)
    hadoopConf.set(prefix + ".password",creds("password"))
    hadoopConf.setBoolean(prefix + ".public",public)
    hadoopConf.set(prefix + ".region",creds("region"))
    hadoopConf.setInt(prefix + ".http.port",8080)
    
    def url(container_name: String, object_name:String) : String= {
        return(urlbuilder.swifturl2d(name= name, container_name,object_name))
    }
}



