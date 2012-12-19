/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil
import javax.xml.transform.*
import javax.xml.transform.stream.*

/////////////////////////////////////////////////////
//
// constants
//
/////////////////////////////////////////////////////

def BASE="target/generated-sources/archetype/"
def ROOT=BASE + "src/main/resources/"

def license_using_xml_comments="""<?xml version="1.0" encoding="UTF-8"?>
<!--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at
  
         http://www.apache.org/licenses/LICENSE-2.0
         
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
-->
"""
/////////////////////////////////////////////////////
//
// update pom.xml's groupId
//
/////////////////////////////////////////////////////

def pomFile=new File(BASE+"pom.xml")

println "updating ${pomFile.path}"

// read file, ignoring XML pragma
def pomFileText = stripXmlPragma(pomFile)

def pomXml = new XmlSlurper(false,true).parseText(pomFileText)
pomXml.groupId='org.apache.isis.archetype'

def pomSmb = new groovy.xml.StreamingMarkupBuilder().bind {
    mkp.declareNamespace("":"http://maven.apache.org/POM/4.0.0")
    mkp.yield(pomXml)
}

def pomTempFile = File.createTempFile("temp",".xml")
pomTempFile.text = indentXml(pomSmb.toString())
def pomXmlText = stripXmlPragma(pomTempFile)


pomFile.text = 
    license_using_xml_comments + 
    pomXmlText



/////////////////////////////////////////////////////
//
// update archetype-metadata.xml
//
/////////////////////////////////////////////////////


def metaDataFile=new File(ROOT+"META-INF/maven/archetype-metadata.xml")

println "updating ${metaDataFile.path}"


// read file, ignoring XML pragma
def metaDataFileText = stripXmlPragma(metaDataFile)

def metaDataXml = new XmlSlurper().parseText(metaDataFileText)
metaDataXml.modules.module.fileSets.fileSet.each { fileSet ->
    if(fileSet.directory=='ide/eclipse') {
        fileSet.@filtered='true'
    }
}

def metaDataSmb = new groovy.xml.StreamingMarkupBuilder().bind {
    mkp.xmlDeclaration()
    mkp.declareNamespace("":"http://maven.apache.org/plugins/maven-archetype-plugin/archetype-descriptor/1.0.0")
    mkp.yield(metaDataXml)
}

def tempFile = File.createTempFile("temp",".xml")
tempFile.text = indentXml(metaDataSmb.toString())
def metaDataXmlText = stripXmlPragma(tempFile)


metaDataFile.text = 
    license_using_xml_comments + 
    metaDataXmlText


/////////////////////////////////////////////////////
//
// update the .launch files
//
/////////////////////////////////////////////////////

new File(ROOT+"archetype-resources/").eachDirRecurse() { dir ->  

    dir.eachFileMatch(~/.*[.]launch/) { launchFile ->  

        println "updating ${launchFile.path}"

        def launchXml = new XmlSlurper().parseText(launchFile.text)
        def projectAttr = launchXml.stringAttribute.find { it.@key=="org.eclipse.jdt.launching.PROJECT_ATTR" }
        String oldValue=projectAttr.@value
        def newValue = oldValue.replaceAll("quickstart-","\\\${rootArtifactId}-")
        projectAttr.@value=newValue

        launchFile.text = """#set( \$symbol_pound = '#' )
#set( \$symbol_dollar = '\$' )
#set( \$symbol_escape = '\\' )
"""
        launchFile.append(XmlUtil.serialize(launchXml))
     }  
}


///////////////////////////////////////////////////
//
// helper methods
//
///////////////////////////////////////////////////

String indentXml(xml) {
    def factory = TransformerFactory.newInstance()
    factory.setAttribute("indent-number", 2);

    Transformer transformer = factory.newTransformer()
    transformer.setOutputProperty(OutputKeys.INDENT, 'yes')
    StreamResult result = new StreamResult(new StringWriter())
    transformer.transform(new StreamSource(new ByteArrayInputStream(xml.toString().bytes)), result)
    return result.writer.toString()
}

String stripXmlPragma(File file) {
    def sw = new StringWriter()
    file.filterLine(sw) { ! (it =~ /^\<\?xml/ ) }
    return sw.toString()
}

