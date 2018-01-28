package sybrix.easygsp2.db.transforms

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import java.lang.reflect.Modifier
import static org.codehaus.groovy.ast.ClassHelper.make

/**
 * Created by dsmith on 8/21/16.
 */

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class FBEntityTransform extends AbstractASTTransformation {

        static final Class MY_CLASS = FBEntity.class;
        static final ClassNode MY_TYPE = make(MY_CLASS);
        static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
        private static final ClassNode STRINGBUILDER_TYPE = make(StringBuilder.class);
        private static final ClassNode INVOKER_TYPE = make(InvokerHelper.class);

        @Override
        void visit(ASTNode[] nodes, SourceUnit source) {
                println "local transform hello-------------------------------------------------"

                init(nodes, source);
                AnnotatedNode parent = (AnnotatedNode) nodes[1];
                AnnotationNode anno = (AnnotationNode) nodes[0];
                if (!MY_TYPE.equals(anno.getClassNode())) return;


                try {
                        println "1"
                        if (parent instanceof ClassNode) {
                                println "2"
                                ClassNode cNode = (ClassNode) parent;


                                        println "3"
                                        // injectIdProperty(classes.get(0))

                                        changeParent(cNode)
                                        //addSetProperty(cNode)
                                        addStaticDAOMethods("delete", getFullName(cNode), cNode)
                                        addStaticDAOMethods("findAll", getFullName(cNode), cNode)
                                        addStaticDAOMethods("find", getFullName(cNode), cNode)
                                        addStaticDAOMethods("list", getFullName(cNode), cNode)

                                        //addDelete(getFullName(cNode), cNode)

                        }

                } catch (Throwable e) {
                        e.printStackTrace();
                }
        }

        private def addStaticDAOMethods(String methodName, String className, ClassNode classNode) {
                def statement = new AstBuilder().buildFromString(
                        "return sybrix.easygsp2.db.firebird.Model." + methodName + "(\"" + className + "\", paramsMap)"
                )[0]

                classNode.addMethod(new MethodNode(methodName, Modifier.PUBLIC | Modifier.STATIC,
                        ClassHelper.OBJECT_TYPE,
                        [new Parameter(ClassHelper.OBJECT_TYPE,
                                'paramsMap')] as Parameter[],
                        [] as ClassNode[],
                        statement)
                )
        }


        private def addDelete(String className, ClassNode classNode) {
                def statement = new AstBuilder().buildFromString(
                        "return this.delete()"
                )[0]

                classNode.addMethod(new MethodNode("delete", Modifier.PUBLIC,
                        ClassHelper.OBJECT_TYPE,
                        [] as Parameter[],
                        [] as ClassNode[],
                        statement)
                )
        }


        public void changeParent(ClassNode classNode) {

                classNode.superClass = new ClassNode(sybrix.easygsp2.db.firebird.Model.class)

                //parent.superClass = new ClassNode(easyom.Model.class)
//                Class cls = Class.forName(getFullName(classNode))
//                Field[] fields = cls.declaredFields
//                for (int i = 0; i < fields.length; i++) {
//                        if (fields[i].getAnnotation(easyom.Id.class) != null) {
//                                Field f = cls.getField('primaryKeys')
//                                List keys = f.get(null)
//                                keys.add(fields[i].name)
//                                 println("adding " + fields[i].name + " to " + getFullName(classNode))
//                        }
//                }

        }

        public void injectIdProperty(ClassNode classNode) {
                //final boolean hasId = GrailsASTUtils.hasOrInheritsProperty(classNode, GrailsDomainClassProperty.IDENTITY);

                //if (!hasId) {
                // inject into furthest relative
                ClassNode parent = getFurthestUnresolvedParent(classNode);


                parent.addProperty('dynamicProperties', Modifier.PRIVATE, new ClassNode([].class), new ListExpression(), null, null);
                //}
        }

        public static ClassNode getFurthestUnresolvedParent(ClassNode classNode) {
                ClassNode parent = classNode.getSuperClass();

                while (parent != null && !getFullName(parent).equals("java.lang.Object") && !parent.isResolved() && !Modifier.isAbstract(parent.getModifiers())) {
                        classNode = parent;
                        parent = parent.getSuperClass();
                }

                return classNode;
        }

        public static String getFullName(ClassNode classNode) {
                return classNode.getName();
        }
}
