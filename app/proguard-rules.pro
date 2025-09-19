# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/swati/Android/Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
# ✅ Keep GSON models
-keep class com.google.gson.** { *; }
-keep class * implements java.io.Serializable

# OneSignal ProGuard rules
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes Annotation
-keepclassmembers enum * { *; }
-keep class com.google.firebase.** { *; }
-keep class com.canhub.cropper.** { *; }
-keep class com.theartofdev.edmodo.cropper.** { *; }

-dontwarn java.awt.Dimension
-dontwarn java.awt.Rectangle
-dontwarn java.rmi.UnexpectedException
-dontwarn org.apache.tools.zip.ZipEntry
-dontwarn org.apache.tools.zip.ZipFile
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn org.spongycastle.cert.X509CertificateHolder
-dontwarn org.spongycastle.cms.CMSEnvelopedData
-dontwarn org.spongycastle.cms.Recipient
-dontwarn org.spongycastle.cms.RecipientId
-dontwarn org.spongycastle.cms.RecipientInformation
-dontwarn org.spongycastle.cms.RecipientInformationStore
-dontwarn org.spongycastle.cms.jcajce.JceKeyTransEnvelopedRecipient
-dontwarn org.spongycastle.cms.jcajce.JceKeyTransRecipient
-dontwarn com.sun.msv.datatype.DatabindableDatatype
-dontwarn com.sun.msv.datatype.SerializationContext
-dontwarn com.sun.msv.datatype.xsd.DatatypeFactory
-dontwarn com.sun.msv.datatype.xsd.TypeIncubator
-dontwarn com.sun.msv.datatype.xsd.XSDatatype
-dontwarn com.sun.msv.datatype.xsd.XSDatatypeImpl
-dontwarn java.awt.Color
-dontwarn java.beans.BeanInfo
-dontwarn java.beans.IntrospectionException
-dontwarn java.beans.Introspector
-dontwarn java.beans.PropertyDescriptor
-dontwarn org.jaxen.VariableContext
-dontwarn org.jaxen.XPath
-dontwarn org.relaxng.datatype.DatatypeException
-dontwarn org.relaxng.datatype.ValidationContext

# Enable optimization
-dontoptimize

# Keep AndroidX and Material Components
-keep class androidx.** { *; }
-keep class com.google.android.material.** { *; }

# Keep ButterKnife (if uncommented in your dependencies)
-keep class butterknife.** { *; }
-keep class com.jakewharton.** { *; }
-dontwarn butterknife.**

# Keep Room database models
-keep class androidx.room.** { *; }
-keep class androidx.sqlite.** { *; }
-dontwarn androidx.room.**

# Keep Firebase Crashlytics
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# Keep Material Ripple, Morphing Button, and Material Dialogs
-keep class com.balysv.** { *; }
-keep class com.github.dmytrodanylyk.** { *; }
-keep class com.afollestad.materialdialogs.** { *; }

# Keep Lottie (Animations)
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# Keep Apache POI (for reading DOC, DOCX)
-keep class org.apache.poi.** { *; }
-dontwarn org.apache.poi.**

# Keep iText PDF Library
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**

# Keep PhotoEditor and Image Cropper
-keep class jp.wasabeef.** { *; }
-keep class com.burhanrashid52.photoeditor.** { *; }
-keep class com.vanniktech.android.imagecropper.** { *; }

# Keep ViewPager Transformer
-keep class com.eftimoff.viewpagertransformers.** { *; }
-dontwarn com.eftimoff.viewpagertransformers.**

# Keep ZXing (QR Code Scanner)
-keep class com.google.zxing.** { *; }
-keep class com.journeyapps.** { *; }
-dontwarn com.google.zxing.**
-dontwarn com.journeyapps.**

# Keep Glide (Image loading)
-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**

# Keep Matisse (Image Picker)
-keep class com.ticherhaz.matisse.** { *; }
-dontwarn com.ticherhaz.matisse.**

# Keep SpongyCastle (Security, Encryption)
-keep class org.spongycastle.** { *; }
-dontwarn org.spongycastle.**

# Keep PDF Viewer
-keep class io.github.afreakyelf.pdfviewer.** { *; }
-dontwarn io.github.afreakyelf.pdfviewer.**

# General rules to keep classes with @Keep annotation
-keep @androidx.annotation.Keep class * { *; }
-keep @com.google.gson.annotations.SerializedName class * { *; }

# Prevent obfuscation of classes that use reflection
-keepattributes *Annotation*

# Keep MainActivity and other entry points
#-keep class com.app.figpdfconvertor.figpdf.** { *; }  # <-- Change to your actual package name

# General ProGuard Optimization
-keep class **.R$* { *; }  # Keep resource files
#-keep class org.apache.xerces.** { *; }
#-keep class javax.xml.parsers.** { *; }
#-keepclassmembers class * {
#    public static javax.xml.parsers.DocumentBuilderFactory newInstance(...);
#}

-dontwarn sun.misc.**
-dontwarn javax.annotation.**
-dontwarn kotlin.**

-dontwarn org.openxmlformats.schemas.drawingml.x2006.chart.impl.CTPlotAreaImpl$1ValAxList
-dontwarn org.openxmlformats.schemas.wordprocessingml.x2006.main.impl.CTBodyImpl$1TblList
-dontwarn org.openxmlformats.schemas.wordprocessingml.x2006.main.impl.CTFtnEdnImpl$1TblList
-dontwarn org.openxmlformats.schemas.wordprocessingml.x2006.main.impl.CTHdrFtrImpl$1TblList
-dontwarn org.openxmlformats.schemas.wordprocessingml.x2006.main.impl.CTPImpl$1RList
-dontwarn org.openxmlformats.schemas.wordprocessingml.x2006.main.impl.CTTcImpl$1TblList



#######
-keep class com.itextpdf.** { *; }
-keep class javax.xml.parsers.** { *; }
-keep class org.w3c.dom.** { *; }
-keep class org.xml.sax.** { *; }


#######
-keep class com.app.figpdfconvertor.figpdf.model.** { *;}
-keep class com.app.figpdfconvertor.figpdf.utils.** { *;}
-keep class com.app.figpdfconvertor.figpdf.funnelss.** { *;}
-keep class com.app.figpdfconvertor.figpdf.interfaces.** { *;}


#######
# Apache POI
-keep class org.apache.poi.** { *; }

# dom4j & xmlbeans
-keep class org.dom4j.** { *; }
-keep class org.apache.xmlbeans.** { *; }

# StAX (Woodstox)
-keep class com.ctc.wstx.** { *; }



######
# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn com.sun.javadoc.ClassDoc
-dontwarn com.sun.javadoc.ConstructorDoc
-dontwarn com.sun.javadoc.Doc
-dontwarn com.sun.javadoc.Doclet
-dontwarn com.sun.javadoc.ExecutableMemberDoc
-dontwarn com.sun.javadoc.FieldDoc
-dontwarn com.sun.javadoc.MethodDoc
-dontwarn com.sun.javadoc.PackageDoc
-dontwarn com.sun.javadoc.Parameter
-dontwarn com.sun.javadoc.ProgramElementDoc
-dontwarn com.sun.javadoc.RootDoc
-dontwarn com.sun.javadoc.SourcePosition
-dontwarn com.sun.javadoc.Tag
-dontwarn com.sun.javadoc.Type
-dontwarn com.sun.tools.javadoc.Main
-dontwarn javax.swing.table.AbstractTableModel
-dontwarn javax.swing.tree.DefaultTreeModel
-dontwarn javax.swing.tree.TreeNode
-dontwarn javax.xml.bind.Element
-dontwarn javax.xml.bind.JAXBContext
-dontwarn javax.xml.bind.Marshaller
-dontwarn javax.xml.bind.Unmarshaller
-dontwarn org.apache.tools.ant.BuildException
-dontwarn org.apache.tools.ant.DirectoryScanner
-dontwarn org.apache.tools.ant.FileScanner
-dontwarn org.apache.tools.ant.Project
-dontwarn org.apache.tools.ant.taskdefs.Jar
-dontwarn org.apache.tools.ant.taskdefs.Javac
-dontwarn org.apache.tools.ant.taskdefs.MatchingTask
-dontwarn org.apache.tools.ant.types.FileSet
-dontwarn org.apache.tools.ant.types.Path$PathElement
-dontwarn org.apache.tools.ant.types.Path
-dontwarn org.apache.tools.ant.types.Reference
-dontwarn org.gjt.xpp.XmlEndTag
-dontwarn org.gjt.xpp.XmlPullParser
-dontwarn org.gjt.xpp.XmlPullParserFactory
-dontwarn org.gjt.xpp.XmlStartTag
-dontwarn org.jaxen.Context
-dontwarn org.jaxen.ContextSupport
-dontwarn org.jaxen.FunctionContext
-dontwarn org.jaxen.JaxenException
-dontwarn org.jaxen.NamespaceContext
-dontwarn org.jaxen.Navigator
-dontwarn org.jaxen.SimpleNamespaceContext
-dontwarn org.jaxen.SimpleVariableContext
-dontwarn org.jaxen.XPathFunctionContext
-dontwarn org.jaxen.dom4j.DocumentNavigator
-dontwarn org.jaxen.dom4j.Dom4jXPath
-dontwarn org.jaxen.pattern.Pattern
-dontwarn org.jaxen.pattern.PatternParser
-dontwarn org.jaxen.saxpath.SAXPathException


# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn com.github.javaparser.ParseResult
-dontwarn com.github.javaparser.ParserConfiguration$LanguageLevel
-dontwarn com.github.javaparser.ParserConfiguration
-dontwarn com.github.javaparser.ast.CompilationUnit
-dontwarn com.github.javaparser.ast.Node
-dontwarn com.github.javaparser.ast.NodeList
-dontwarn com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
-dontwarn com.github.javaparser.ast.body.MethodDeclaration
-dontwarn com.github.javaparser.ast.body.Parameter
-dontwarn com.github.javaparser.ast.body.TypeDeclaration
-dontwarn com.github.javaparser.ast.expr.SimpleName
-dontwarn com.github.javaparser.ast.type.PrimitiveType
-dontwarn com.github.javaparser.ast.type.ReferenceType
-dontwarn com.github.javaparser.ast.type.Type
-dontwarn com.github.javaparser.ast.type.TypeParameter
-dontwarn com.github.javaparser.resolution.MethodUsage
-dontwarn com.github.javaparser.resolution.SymbolResolver
-dontwarn com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration
-dontwarn com.github.javaparser.resolution.declarations.ResolvedParameterDeclaration
-dontwarn com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration
-dontwarn com.github.javaparser.resolution.types.ResolvedType
-dontwarn com.github.javaparser.symbolsolver.JavaSymbolSolver
-dontwarn com.github.javaparser.symbolsolver.model.resolution.TypeSolver
-dontwarn com.github.javaparser.symbolsolver.resolution.typesolvers.ClassLoaderTypeSolver
-dontwarn com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
-dontwarn com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver
-dontwarn com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
-dontwarn com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
-dontwarn com.github.javaparser.utils.CollectionStrategy
-dontwarn com.github.javaparser.utils.ProjectRoot
-dontwarn com.github.javaparser.utils.SourceRoot
-dontwarn com.sun.org.apache.xml.internal.resolver.CatalogManager
-dontwarn com.sun.org.apache.xml.internal.resolver.tools.CatalogResolver
-dontwarn java.awt.Shape
-dontwarn javax.xml.stream.Location
-dontwarn javax.xml.stream.XMLEventFactory
-dontwarn javax.xml.stream.XMLInputFactory
-dontwarn javax.xml.stream.XMLOutputFactory
-dontwarn javax.xml.stream.XMLStreamException
-dontwarn javax.xml.stream.XMLStreamReader
-dontwarn javax.xml.stream.XMLStreamWriter
-dontwarn javax.xml.stream.util.StreamReaderDelegate
-dontwarn net.sf.saxon.Configuration
-dontwarn net.sf.saxon.dom.DOMNodeWrapper
-dontwarn net.sf.saxon.dom.DocumentWrapper
-dontwarn net.sf.saxon.dom.NodeOverNodeInfo
-dontwarn net.sf.saxon.lib.ConversionRules
-dontwarn net.sf.saxon.ma.map.HashTrieMap
-dontwarn net.sf.saxon.om.GroundedValue
-dontwarn net.sf.saxon.om.Item
-dontwarn net.sf.saxon.om.NodeInfo
-dontwarn net.sf.saxon.om.Sequence
-dontwarn net.sf.saxon.om.SequenceTool
-dontwarn net.sf.saxon.om.StructuredQName
-dontwarn net.sf.saxon.query.DynamicQueryContext
-dontwarn net.sf.saxon.query.StaticQueryContext
-dontwarn net.sf.saxon.query.XQueryExpression
-dontwarn net.sf.saxon.str.StringView
-dontwarn net.sf.saxon.str.UnicodeString
-dontwarn net.sf.saxon.sxpath.IndependentContext
-dontwarn net.sf.saxon.sxpath.XPathDynamicContext
-dontwarn net.sf.saxon.sxpath.XPathEvaluator
-dontwarn net.sf.saxon.sxpath.XPathExpression
-dontwarn net.sf.saxon.sxpath.XPathStaticContext
-dontwarn net.sf.saxon.sxpath.XPathVariable
-dontwarn net.sf.saxon.tree.wrapper.VirtualNode
-dontwarn net.sf.saxon.type.BuiltInAtomicType
-dontwarn net.sf.saxon.type.ConversionResult
-dontwarn net.sf.saxon.value.AnyURIValue
-dontwarn net.sf.saxon.value.AtomicValue
-dontwarn net.sf.saxon.value.BigDecimalValue
-dontwarn net.sf.saxon.value.BigIntegerValue
-dontwarn net.sf.saxon.value.BooleanValue
-dontwarn net.sf.saxon.value.CalendarValue
-dontwarn net.sf.saxon.value.DateTimeValue
-dontwarn net.sf.saxon.value.DateValue
-dontwarn net.sf.saxon.value.DoubleValue
-dontwarn net.sf.saxon.value.DurationValue
-dontwarn net.sf.saxon.value.FloatValue
-dontwarn net.sf.saxon.value.GDateValue
-dontwarn net.sf.saxon.value.GDayValue
-dontwarn net.sf.saxon.value.GMonthDayValue
-dontwarn net.sf.saxon.value.GMonthValue
-dontwarn net.sf.saxon.value.GYearMonthValue
-dontwarn net.sf.saxon.value.GYearValue
-dontwarn net.sf.saxon.value.HexBinaryValue
-dontwarn net.sf.saxon.value.Int64Value
-dontwarn net.sf.saxon.value.ObjectValue
-dontwarn net.sf.saxon.value.QNameValue
-dontwarn net.sf.saxon.value.SaxonDuration
-dontwarn net.sf.saxon.value.SaxonXMLGregorianCalendar
-dontwarn net.sf.saxon.value.StringValue
-dontwarn net.sf.saxon.value.TimeValue
-dontwarn org.apache.maven.model.Resource
-dontwarn org.apache.maven.plugin.AbstractMojo
-dontwarn org.apache.maven.plugin.MojoExecutionException
-dontwarn org.apache.maven.plugin.MojoFailureException
-dontwarn org.apache.maven.plugin.logging.Log
-dontwarn org.apache.maven.plugins.annotations.LifecyclePhase
-dontwarn org.apache.maven.plugins.annotations.Mojo
-dontwarn org.apache.maven.plugins.annotations.Parameter
-dontwarn org.apache.maven.project.MavenProject
-dontwarn org.osgi.framework.Bundle
-dontwarn org.osgi.framework.BundleContext
-dontwarn org.osgi.framework.FrameworkUtil
-dontwarn org.osgi.framework.ServiceReference
-dontwarn org.tukaani.xz.ARMOptions
-dontwarn org.tukaani.xz.ARMThumbOptions
-dontwarn org.tukaani.xz.FilterOptions
-dontwarn org.tukaani.xz.IA64Options
-dontwarn org.tukaani.xz.LZMA2Options
-dontwarn org.tukaani.xz.PowerPCOptions
-dontwarn org.tukaani.xz.SPARCOptions
-dontwarn org.tukaani.xz.X86Options

