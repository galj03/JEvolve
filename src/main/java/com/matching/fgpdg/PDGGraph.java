package com.matching.fgpdg;

import com.matching.fgpdg.nodes.*;
import com.matching.fgpdg.nodes.ast.AlphanumericHole;
import com.matching.fgpdg.nodes.ast.LazyHole;
import com.utils.Assertions;

import com.utils.JavaASTUtil;
import org.eclipse.jdt.core.dom.*;

import java.io.Serializable;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static com.matching.fgpdg.nodes.PDGDataEdge.Type.*;

public class PDGGraph implements Serializable {
    private static final long serialVersionUID = -5128703931982211886L;
    protected PDGDataNode[] parameters;
    protected PDGNode entryNode, endNode;
    protected HashSet<PDGNode> nodes = new HashSet<PDGNode>();
    protected HashSet<PDGNode> statementNodes = new HashSet<>();
    protected HashSet<PDGDataNode> dataSources = new HashSet<>();
    protected HashSet<PDGHoleNode> holeDataSources = new HashSet<>();//TODO: investigate usage
    protected HashSet<PDGNode> statementSinks = new HashSet<>();
    protected HashSet<PDGNode> statementSources = new HashSet<>();
    protected HashSet<PDGNode> sinks = new HashSet<PDGNode>();
    protected HashSet<PDGNode> breaks = new HashSet<>();
    protected HashSet<PDGNode> returns = new HashSet<>();
    protected HashSet<PDGNode> changedNodes = new HashSet<>();
    private PDGBuildingContext context;
    private HashMap<String, HashSet<PDGDataNode>> defStore = new HashMap<>();
    private HashMap<String, HashSet<PDGHoleNode>> defHoleStore = new HashMap<>();
    private HashMap<Integer, PDGNode> idPDG = new HashMap<>();

    public PDGGraph(MethodDeclaration md, PDGBuildingContext context) {
        this(context);
        context.addScope();
        this.context.setMethod(md, false);
        int numOfParameters = 0;
//        parameters = new PDGDataNode[md.parameters().size()];
        if (Modifier.isStatic(md.getModifiers()))
            parameters = new PDGDataNode[md.parameters().size()];
        else {
            parameters = new PDGDataNode[md.parameters().size() + 1];
            parameters[numOfParameters++] = new PDGDataNode(
                    null, ASTNode.THIS_EXPRESSION, "this", "this", "this");
        }

        entryNode = new PDGEntryNode(md, ASTNode.METHOD_DECLARATION, "START");
        nodes.add(entryNode);
        statementNodes.add(entryNode);
        for (int i = 0; i < md.parameters().size(); i++) {
            SingleVariableDeclaration d = (SingleVariableDeclaration) md
                    .parameters().get(i);
            String id = d.getName().getIdentifier();
            mergeSequential(buildPDG(entryNode, "", d));
            String[] info = context.getLocalVariableInfo(id);  //TODO handel the Type information
            this.parameters[numOfParameters++] = new PDGDataNode(
                    d.getName(), ASTNode.SIMPLE_NAME, info[0], info[1],
                    "PARAM_" + d.getName().getIdentifier(), false, true);
        }
        context.pushTry();
        if (md.getBody() != null) {
            Block block = md.getBody();
            if (!block.statements().isEmpty())
                mergeSequential(buildPDG(entryNode, "", block));
        }
        if (!context.interprocedural)
            statementSinks.addAll(context.popTry());
        adjustReturnNodes();
        adjustControlEdges();
        context.removeScope();
    }

    public PDGNode getPDGNode(int id) {
        if (idPDG.size() == 0) {
            for (PDGNode node : nodes) {
                idPDG.put(node.getId(), node);
            }
        }
        return idPDG.get(id);
    }

    //TODO: I don't think this is relevant in Java, but rethink it later
//    public PDGGraph(Module md, PDGBuildingContext context) {
//        this.context = context;
//        context.addScope();
//        entryNode = new PDGEntryNode(md, PyObject.MODULE, "START");
//        nodes.add(entryNode);
//        statementNodes.add(entryNode);
//        for (stmt stmt : md.getInternalBody()) {
//            if (stmt instanceof ImportFrom || stmt instanceof Import)
//                continue;
//            mergeSequential(Objects.requireNonNull(buildPDG(entryNode, "", stmt)));
//        }
//        adjustReturnNodes();
//        adjustControlEdges();
//        HashSet<PDGNode> toRemove = new HashSet<PDGNode>();
//        for (PDGNode node : nodes) {
//            if (node instanceof PDGEntryNode && node.getLabel().equals("START")) {
//                for (PDGEdge edge : node.getOutEdges()) {
//                    edge.getTarget().getInEdges().remove(edge);
//                }
//                toRemove.add(node);
//            } else if (node instanceof PDGEntryNode && node.getLabel().equals("END")) {
//                for (PDGEdge edge : node.getInEdges()) {
//                    edge.getSource().getOutEdges().remove(edge);
//                }
//                toRemove.add(node);
//            }
//        }
//        toRemove.forEach(x -> nodes.remove(x));
//
//        context.removeScope();
//
//    }

    public PDGGraph(PDGBuildingContext context, PDGNode node) {
        this(context);
        init(node);
    }

    public PDGGraph(PDGBuildingContext context) {
        this.context = context;
    }

    private static <E> void update(HashMap<String, HashSet<E>> target,
                                   HashMap<String, HashSet<E>> source) {
        for (String key : source.keySet()) {
            HashSet<E> s = source.get(key);
            if (s.contains(null)) {
                if (target.containsKey(key)) {
                    s.remove(null);
                    target.get(key).addAll(new HashSet<E>(s));
                } else
                    target.put(key, new HashSet<E>(s));
            } else
                target.put(key, new HashSet<E>(s));
        }
    }

    private static <E> void clear(HashMap<String, HashSet<E>> map) {
        for (String key : map.keySet())
            map.get(key).clear();
        map.clear();
    }

    private static <E> void add(HashMap<String, HashSet<E>> target,
                                HashMap<String, HashSet<E>> source) {
        for (String key : source.keySet())
            if (target.containsKey(key))
                target.get(key).addAll(new HashSet<E>(source.get(key)));
            else
                target.put(key, new HashSet<E>(source.get(key)));
    }

    public HashSet<PDGNode> getNodes() {
        return nodes;
    }

    private void init(PDGNode node) {
        if (node instanceof PDGDataNode && !node.isLiteral())
            dataSources.add((PDGDataNode) node);
        if (node instanceof PDGHoleNode)
            holeDataSources.add((PDGHoleNode) node);
        sinks.add(node);
        nodes.add(node);
        if (node.isStatement()) {
            statementNodes.add(node);
            statementSources.add(node);
            statementSinks.add(node);
        }
    }

    private boolean isEmpty() {
        return nodes.isEmpty();
    }

    private void updateDefStore(HashMap<String, HashSet<PDGDataNode>> store) {
        update(defStore, store);
    }

    private void updateHoleDefStore(HashMap<String, HashSet<PDGHoleNode>> store) {
        update(defHoleStore, store);
    }

    //TODO: are holes only used here?
    private void mergeSequential(PDGGraph pdg) {
        if (pdg.statementNodes.isEmpty())
            return;
        for (PDGDataNode source : new HashSet<>(pdg.dataSources)) {
            HashSet<PDGDataNode> defs = defStore.get(source.getKey());
            if (defs != null) {
                for (PDGDataNode def : defs)
                    if (def != null)
                        new PDGDataEdge(def, source, REFERENCE);
                if (!defs.contains(null))
                    pdg.dataSources.remove(source);
            }
        }
        for (PDGHoleNode source : new HashSet<>(pdg.holeDataSources)) {
            HashSet<PDGHoleNode> defs = defHoleStore.get(source.getKey());
            if (defs != null) {
                for (PDGHoleNode def : defs)
                    if (def != null)
                        new PDGDataEdge(def, source, REFERENCE);
                if (!defs.contains(null))
                    pdg.holeDataSources.remove(source);
            }
        }


        for (Map.Entry<String, HashSet<PDGDataNode>> sourcedefinitions : pdg.defStore.entrySet()) {
            for (PDGDataNode sourceNode : sourcedefinitions.getValue()) {
                if (defStore.get(sourcedefinitions.getKey()) != null) {
                    for (PDGDataNode dataNode : defStore.get(sourcedefinitions.getKey())) {
                        if (sourceNode != null && dataNode != null) {
                            new PDGDataEdge(dataNode, sourceNode, RE_DEFINITION);
                        }

                    }
                }

            }
        }

        for (Map.Entry<String, HashSet<PDGHoleNode>> sourcedefinitions : pdg.defHoleStore.entrySet()) {
            for (PDGHoleNode sourceNode : sourcedefinitions.getValue()) {
                if (defHoleStore.get(sourcedefinitions.getKey()) != null) {
                    for (PDGHoleNode dataNode : defHoleStore.get(sourcedefinitions.getKey())) {
                        if (sourceNode != null && dataNode != null) {
                            new PDGDataEdge(dataNode, sourceNode, RE_DEFINITION);
                        }

                    }
                }

            }
        }

        updateDefStore(pdg.defStore);
        updateHoleDefStore(pdg.defHoleStore);
        for (PDGNode sink : statementSinks) {
            for (PDGNode source : pdg.statementSources) {
                new PDGDataEdge(sink, source, PDGDataEdge.Type.DEPENDENCE);
            }
        }


        this.dataSources.addAll(pdg.dataSources);
        this.holeDataSources.addAll(pdg.holeDataSources);
        this.sinks.clear();
        this.statementSinks.clear();
        this.statementSinks.addAll(pdg.statementSinks);
        this.nodes.addAll(pdg.nodes);
        this.statementNodes.addAll(pdg.statementNodes);
        this.breaks.addAll(pdg.breaks);
        this.returns.addAll(pdg.returns);
        pdg.clear();
    }

    private void mergeSequentialHoleData(PDGHoleNode next, PDGDataEdge.Type type) {
        if (next.isStatement())
            for (PDGNode sink : statementSinks) {
                new PDGDataEdge(sink, next, PDGDataEdge.Type.DEPENDENCE);
            }
        if (type == PDGDataEdge.Type.DEFINITION) {
            HashSet<PDGHoleNode> ns = new HashSet<>();
            ns.add(next);
            defHoleStore.put(next.getKey(), ns);
        } else if (type == QUALIFIER) {
            holeDataSources.add((PDGHoleNode) next);
        } else if (type != REFERENCE && next.isDataNode()) {
            HashSet<PDGHoleNode> ns = defHoleStore.get(next.getKey());
            if (ns != null)
                for (PDGHoleNode def : ns) {
                    if (def.isDataNode())
                        new PDGDataEdge(def, next, REFERENCE);
                }
        }
        for (PDGNode node : sinks)
            new PDGDataEdge(node, next, type);
        sinks.clear();
        sinks.add(next);
        if (nodes.isEmpty() && next.isDataNode())
            holeDataSources.add(next);
        nodes.add(next);
        if (next.isStatement()) {
            statementNodes.add(next);
            if (statementSources.isEmpty())
                statementSources.add(next);
            statementSinks.clear();
            statementSinks.add(next);
        }

    }

    private void mergeSequentialData(PDGNode next, PDGDataEdge.Type type) {
        if (next.isStatement())
            for (PDGNode sink : statementSinks) {
                new PDGDataEdge(sink, next, PDGDataEdge.Type.DEPENDENCE);
            }
        if (type == PDGDataEdge.Type.DEFINITION) {
            HashSet<PDGDataNode> ns = new HashSet<>();
            ns.add((PDGDataNode) next);
            defStore.put(next.getKey(), ns);
        } else if (type == QUALIFIER) {
            dataSources.add((PDGDataNode) next);
        } else if (type != REFERENCE && next instanceof PDGDataNode) {
            HashSet<PDGDataNode> ns = defStore.get(next.getKey());
            if (ns != null)
                for (PDGDataNode def : ns)
                    new PDGDataEdge(def, next, REFERENCE);
        }
        for (PDGNode node : sinks)
            new PDGDataEdge(node, next, type);
        sinks.clear();
        sinks.add(next);
        if (nodes.isEmpty() && next instanceof PDGDataNode)
            dataSources.add((PDGDataNode) next);
        nodes.add(next);
        if (next.isStatement()) {
            statementNodes.add(next);
            if (statementSources.isEmpty())
                statementSources.add(next);
            statementSinks.clear();
            statementSinks.add(next);
        }
    }

    private void mergeSequentialDataNoUpdatetoSinks(PDGNode next, PDGDataEdge.Type type) {
        if (next.isStatement())
            for (PDGNode sink : statementSinks) {
                new PDGDataEdge(sink, next, PDGDataEdge.Type.DEPENDENCE);
            }
        if (type == PDGDataEdge.Type.DEFINITION) {
            HashSet<PDGDataNode> ns = new HashSet<>();
            ns.add((PDGDataNode) next);
            defStore.put(next.getKey(), ns);
        } else if (type == QUALIFIER) {
            dataSources.add((PDGDataNode) next);
        } else if (type != REFERENCE && next instanceof PDGDataNode) {
            HashSet<PDGDataNode> ns = defStore.get(next.getKey());
            if (ns != null)
                for (PDGDataNode def : ns)
                    new PDGDataEdge(def, next, REFERENCE);
        }
        for (PDGNode node : sinks)
            new PDGDataEdge(node, next, type);
        if (nodes.isEmpty() && next instanceof PDGDataNode)
            dataSources.add((PDGDataNode) next);
        nodes.add(next);
        if (next.isStatement()) {
            statementNodes.add(next);
            if (statementSources.isEmpty())
                statementSources.add(next);
            statementSinks.add(next);
        }
    }

    private void mergeBranches(PDGGraph... pdgs) {
        HashMap<String, HashSet<PDGDataNode>> defStore = new HashMap<>();
        HashMap<String, Integer> defCounts = new HashMap<>();
        sinks.clear();
        statementSinks.clear();
        for (PDGGraph pdg : pdgs) {
            nodes.addAll(pdg.nodes);
            statementNodes.addAll(pdg.statementNodes);
            sinks.addAll(pdg.sinks);
            statementSinks.addAll(pdg.statementSinks);
            for (PDGDataNode source : new HashSet<PDGDataNode>(pdg.dataSources)) {
                HashSet<PDGDataNode> defs = this.defStore.get(source.getKey());
                if (defs != null) {
                    for (PDGDataNode def : defs)
                        if (def != null)
                            new PDGDataEdge(def, source, REFERENCE);
                    if (!defs.contains(null))
                        pdg.dataSources.remove(source);
                }
            }
            for (PDGHoleNode source : new HashSet<PDGHoleNode>(pdg.holeDataSources)) {
                HashSet<PDGHoleNode> defs = this.defHoleStore.get(source.getKey());
                if (defs != null) {
                    for (PDGHoleNode def : defs)
                        if (def != null && def.isDataNode())
                            new PDGDataEdge(def, source, REFERENCE);
                    if (!defs.contains(null))
                        pdg.holeDataSources.remove(source);
                }
            }
            holeDataSources.addAll(pdg.holeDataSources);
            dataSources.addAll(pdg.dataSources);
            // statementSources.addAll(pdg.statementSources);
            breaks.addAll(pdg.breaks);
            returns.addAll(pdg.returns);
        }
        for (PDGGraph pdg : pdgs) {
            HashMap<String, HashSet<PDGDataNode>> localStore = copyDefStore();
            updateDefStore(localStore, defCounts, pdg.defStore);
            add(defStore, localStore);
            pdg.clear();
        }
        for (String key : defCounts.keySet())
            if (defCounts.get(key) < pdgs.length)
                defStore.get(key).add(null);
        clearDefStore();
        this.defStore = defStore;
    }

    private void clear() {
        nodes.clear();
        statementNodes.clear();
        dataSources.clear();
        holeDataSources.clear();
        statementSources.clear();
        sinks.clear();
        statementSinks.clear();
        breaks.clear();
        returns.clear();
        clearDefStore();
    }

    private void adjustReturnNodes() {
        sinks.addAll(returns);
        statementSinks.addAll(returns);
        returns.clear();
        endNode = new PDGEntryNode(null, ASTNode.METHOD_DECLARATION, "END");
        for (PDGNode sink : statementSinks)
            new PDGDataEdge(sink, endNode, PDGDataEdge.Type.DEPENDENCE);
        sinks.clear();
        statementSinks.clear();
        nodes.add(endNode);
        statementNodes.remove(entryNode);
    }

    private void adjustControlEdges() {
        for (PDGNode node : statementNodes) {
            ArrayList<PDGNode> ens = node.getIncomingEmptyNodes();
            if (ens.size() == 1 && node.getInDependences().size() == 1) {
                PDGNode en = ens.get(0);
                if (node.getControl() != en.getControl()) {
                    node.getControl().adjustControl(node, en);
                }
            }
        }
    }

    private ArrayList<PDGDataNode> getDefinitions() {
        ArrayList<PDGDataNode> defs = new ArrayList<>();
        for (PDGNode node : sinks)
            if (node instanceof PDGDataNode && node.isDefinition())
                defs.add((PDGDataNode) node);
        return defs;
    }

    private ArrayList<PDGHoleNode> getHoleDefinitions() {
        ArrayList<PDGHoleNode> defs = new ArrayList<>();
        for (PDGNode node : sinks)
            if (node instanceof PDGHoleNode && node.isDefinition())
                defs.add((PDGHoleNode) node);
        return defs;
    }

    private ArrayList<PDGActionNode> getReturns() {
        ArrayList<PDGActionNode> nodes = new ArrayList<>();
        for (PDGNode node : statementSinks)
            if (node.getAstNodeType() == ASTNode.RETURN_STATEMENT)
                nodes.add((PDGActionNode) node);
        return nodes;
    }

    private PDGGraph buildArgumentPDG(PDGNode control, String branch,
                                      ASTNode exp) {
        PDGGraph pdg = buildPDG(control, branch, exp);
        if (pdg == null) {
            System.out.println();
            buildPDG(control, branch, exp);
        }
        if (pdg.isEmpty())
            return pdg;
        if (pdg.nodes.size() == 1)
            for (PDGNode node : pdg.nodes)
                if (node instanceof PDGDataNode)
                    return pdg;
                else if (node instanceof PDGAlphHole && ((PDGAlphHole) node).isDataNode()) {
                    return pdg;
                }
        ArrayList<PDGDataNode> defs = pdg.getDefinitions();
        if (!defs.isEmpty()) {
            PDGDataNode def = defs.get(0);
            pdg.mergeSequentialData(new PDGDataNode(null, def.getAstNodeType(), def.getKey(),
                    ((PDGDataNode) def).getDataType(), ((PDGDataNode) def).getDataName(),
                    def.isField(), false), REFERENCE);
            return pdg;
        }
        ArrayList<PDGActionNode> rets = pdg.getReturns();
        if (rets.size() > 0) {
            PDGDataNode dummy = new PDGDataNode(null, ASTNode.SIMPLE_NAME,
                    PDGNode.PREFIX_DUMMY + exp.getStartPosition() + "_"
                            + exp.getLength(), rets.get(0).getDataType(), PDGNode.PREFIX_DUMMY, false, true);
            for (PDGActionNode ret : rets) {
                ret.setAstNodeType(ASTNode.ASSIGNMENT);
                ret.setName("=");
                pdg.extend(ret, new PDGDataNode(dummy), PDGDataEdge.Type.DEFINITION);
            }
            pdg.mergeSequentialData(new PDGDataNode(null, dummy.getAstNodeType(),
                    dummy.getKey(), dummy.getDataType(), dummy.getDataName()), REFERENCE);
            return pdg;
        }
        PDGNode node = pdg.getOnlyOut();
        if (node instanceof PDGDataNode)
            return pdg;
//        int startChar = 0;
//        int length = 0;
//        if (exp instanceof expr) {
//            startChar = ((expr) exp).getCharStartIndex();
//            length = ((expr) exp).getCharStopIndex() - ((expr) exp).getCharStartIndex();
//        } else if (exp instanceof stmt) {
//            startChar = ((stmt) exp).getCharStartIndex();
//            length = ((stmt) exp).getCharStopIndex() - ((stmt) exp).getCharStartIndex();
//        }
//        else if (exp instanceof Index){
//            startChar = ((Index) exp).getCharStartIndex();
//            length = ((Index) exp).getCharStopIndex() - ((Index) exp).getCharStartIndex();
//        }
//        else {
//            Assertions.UNREACHABLE();
//        }
//        PDGDataNode dummy = new PDGDataNode(null, PyObject.NAME,
//                PDGNode.PREFIX_DUMMY + startChar + "_"
//                        + length, node.getDataType(), PDGNode.PREFIX_DUMMY, false, true);
//        pdg.mergeSequentialData(new PDGActionNode(control, branch,
//                null, PyObject.ASSIGN, null, null, "="), PARAMETER);
//        pdg.mergeSequentialData(dummy, PDGDataEdge.Type.DEFINITION);
//        pdg.mergeSequentialData(new PDGDataNode(null, dummy.getAstNodeType(), dummy.getKey(),
//                dummy.getDataType(), dummy.getDataName()), REFERENCE);

        //TODO: is this needed? - probably not, it was commented out before as well
//        PDGDataNode dummy = new PDGDataNode(null, ASTNode.SIMPLE_NAME,
//                PDGNode.PREFIX_DUMMY + exp.getStartPosition() + "_"
//                        + exp.getLength(), node.getDataType(), PDGNode.PREFIX_DUMMY, false, true);
//        pdg.mergeSequentialData(new PDGActionNode(control, branch,
//                null, ASTNode.ASSIGNMENT, null, null, "="), PARAMETER);
//        pdg.mergeSequentialData(dummy, DEFINITION);
//        pdg.mergeSequentialData(new PDGDataNode(null, dummy.getAstNodeType(), dummy.getKey(),
//                dummy.getDataType(), dummy.getDataName()), REFERENCE);

        return pdg;
    }

    private void clearDefStore() {
        clear(defStore);
    }

    //TODO (LATER): match buildPDG methods to include holes where needed
    //TODO: test if it works well together
    private PDGGraph buildPDG(PDGNode control, String branch, ASTNode node) {
        if (node instanceof ArrayAccess)
            return buildPDG(control, branch, (ArrayAccess) node);
        if (node instanceof ArrayCreation)
            return buildPDG(control, branch, (ArrayCreation) node);
        if (node instanceof ArrayInitializer)
            return buildPDG(control, branch, (ArrayInitializer) node);
        if (node instanceof AssertStatement)
            return buildPDG(control, branch, (AssertStatement) node);
        if (node instanceof Assignment)
            return buildPDG(control, branch, (Assignment) node);
        if (node instanceof Block)
            return buildPDG(control, branch, (Block) node);
        if (node instanceof BooleanLiteral)
            return buildPDG(control, branch, (BooleanLiteral) node);
        if (node instanceof BreakStatement)
            return buildPDG(control, branch, (BreakStatement) node);
        if (node instanceof CastExpression)
            return buildPDG(control, branch, (CastExpression) node);
        if (node instanceof CatchClause)
            return buildPDG(control, branch, (CatchClause) node);
        if (node instanceof CharacterLiteral)
            return buildPDG(control, branch, (CharacterLiteral) node);
        if (node instanceof ClassInstanceCreation)
            return buildPDG(control, branch, (ClassInstanceCreation) node);
        if (node instanceof ConditionalExpression)
            return buildPDG(control, branch, (ConditionalExpression) node);
        if (node instanceof ConstructorInvocation)
            return buildPDG(control, branch, (ConstructorInvocation) node);
        if (node instanceof ContinueStatement)
            return buildPDG(control, branch, (ContinueStatement) node);
        if (node instanceof DoStatement)
            return buildPDG(control, branch, (DoStatement) node);
        if (node instanceof EnhancedForStatement)
            return buildPDG(control, branch, (EnhancedForStatement) node);
        if (node instanceof EnhancedForStatementWithElse)
            return buildPDG(control, branch, (EnhancedForStatementWithElse) node);
        if (node instanceof ExpressionStatement)
            return buildPDG(control, branch, (ExpressionStatement) node);
        if (node instanceof FieldAccess)
            return buildPDG(control, branch, (FieldAccess) node);
        if (node instanceof ForStatement)
            return buildPDG(control, branch, (ForStatement) node);
        if (node instanceof IfStatement)
            return buildPDG(control, branch, (IfStatement) node);
        if (node instanceof InfixExpression)
            return buildPDG(control, branch, (InfixExpression) node);
        if (node instanceof Initializer)
            return buildPDG(control, branch, (Initializer) node);
        if (node instanceof InstanceofExpression)
            return buildPDG(control, branch, (InstanceofExpression) node);
        if (node instanceof LabeledStatement)
            return buildPDG(control, branch, (LabeledStatement) node);
        if (node instanceof MethodDeclaration)
            return buildPDG(control, branch, (MethodDeclaration) node);
        if (node instanceof MethodInvocation)
            return buildPDG(control, branch, (MethodInvocation) node);
        if (node instanceof NullLiteral)
            return buildPDG(control, branch, (NullLiteral) node);
        if (node instanceof NumberLiteral)
            return buildPDG(control, branch, (NumberLiteral) node);
        if (node instanceof ParenthesizedExpression)
            return buildPDG(control, branch, (ParenthesizedExpression) node);
        if (node instanceof PostfixExpression)
            return buildPDG(control, branch, (PostfixExpression) node);
        if (node instanceof PrefixExpression)
            return buildPDG(control, branch, (PrefixExpression) node);
        if (node instanceof QualifiedName)
            return buildPDG(control, branch, (QualifiedName) node);
        if (node instanceof ReturnStatement)
            return buildPDG(control, branch, (ReturnStatement) node);
        if (node instanceof SimpleName)
            return buildPDG(control, branch, (SimpleName) node);
        if (node instanceof SingleVariableDeclaration)
            return buildPDG(control, branch, (SingleVariableDeclaration) node);
        if (node instanceof StringLiteral)
            return buildPDG(control, branch, (StringLiteral) node);
        if (node instanceof SuperConstructorInvocation)
            return buildPDG(control, branch, (SuperConstructorInvocation) node);
        if (node instanceof SuperFieldAccess)
            return buildPDG(control, branch, (SuperFieldAccess) node);
        if (node instanceof SuperMethodInvocation)
            return buildPDG(control, branch, (SuperMethodInvocation) node);
        if (node instanceof SwitchCase)
            return buildPDG(control, branch, (SwitchCase) node);
        if (node instanceof SwitchStatement)
            return buildPDG(control, branch, (SwitchStatement) node);
        if (node instanceof SynchronizedStatement)
            return buildPDG(control, branch, (SynchronizedStatement) node);
        if (node instanceof ThisExpression)
            return buildPDG(control, branch, (ThisExpression) node);
        if (node instanceof ThrowStatement)
            return buildPDG(control, branch, (ThrowStatement) node);
        if (node instanceof TryStatement)
            return buildPDG(control, branch, (TryStatement) node);
        if (node instanceof TypeLiteral)
            return buildPDG(control, branch, (TypeLiteral) node);
        if (node instanceof VariableDeclarationExpression)
            return buildPDG(control, branch,
                    (VariableDeclarationExpression) node);
        if (node instanceof VariableDeclarationFragment)
            return buildPDG(control, branch, (VariableDeclarationFragment) node);
        if (node instanceof VariableDeclarationStatement)
            return buildPDG(control, branch,
                    (VariableDeclarationStatement) node);
        if (node instanceof WhileStatement)
            return buildPDG(control, branch, (WhileStatement) node);
        if (node instanceof PyWithStatement)
            return buildPDG(control, branch, (PyWithStatement) node);
        if (node instanceof PyInExpression)
            return buildPDG(control, branch, (PyInExpression) node);
        if (node instanceof PyNotInExpression)
            return buildPDG(control, branch, (PyNotInExpression) node);
        if (node instanceof PyYieldReturnStatement)
            return buildPDG(control, branch, (PyYieldReturnStatement) node);
        if (node instanceof PyGenerator)
            return buildPDG(control, branch, (PyGenerator) node);
        if (node instanceof PyComparator)
            return buildPDG(control, branch, (PyComparator) node);
        if (node instanceof PyTupleExpression)
            return buildPDG(control, branch, (PyTupleExpression) node);
        if (node instanceof PySetComprehension)
            return buildPDG(control, branch, (PySetComprehension) node);
        if (node instanceof PyListComprehension)
            return buildPDG(control, branch, (PyListComprehension) node);
        if (node instanceof PyDictComprehension)
            return buildPDG(control, branch, (PyDictComprehension) node);
        if (node instanceof PyNonLocalStatement)
            return buildPDG(control, branch, (PyNonLocalStatement) node);
//        if (node instanceof AlphanumericHole)
//            return buildPDG(control, branch, (AlphanumericHole) node);
//        if (node instanceof LazyHole)
//            return buildPDG(control, branch, (LazyHole) node);
//        return new PDGGraph(context);
        if (node == null)
            return new PDGGraph(context);
        Assertions.UNREACHABLE(node.getClass().toString());
        return null;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              WhileStatement astNode) {
        context.addScope();
        PDGGraph pdg = buildArgumentPDG(control, branch,
                astNode.getExpression());
        PDGControlNode node = new PDGControlNode(control, branch,
                astNode, astNode.getNodeType());
        pdg.mergeSequentialData(node, PDGDataEdge.Type.CONDITION);
        PDGGraph ebg = new PDGGraph(context, new PDGActionNode(node, "T",
                null, ASTNode.EMPTY_STATEMENT, null, null, "empty"));
        PDGGraph bg = buildPDG(node, "T", astNode.getBody());
        if (!bg.isEmpty())
            ebg.mergeSequential(bg);
        PDGGraph eg = new PDGGraph(context, new PDGActionNode(node, "F",
                null, ASTNode.EMPTY_STATEMENT, null, null, "empty"));
        pdg.mergeBranches(ebg, eg);
        /*
         * pdg.sinks.remove(node); pdg.statementSinks.remove(node);
         */
        pdg.adjustBreakNodes("");
        context.removeScope();
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              PyWithStatement astNode){
        context.addScope();
        PDGGraph pdg = buildArgumentPDG(control, branch,
                astNode.getExpression());
        PDGControlNode node = new PDGControlNode(control, branch,
                astNode, astNode.getNodeType());

        pdg.mergeSequentialData(node, PDGDataEdge.Type.CONDITION);
        PDGGraph ebg = new PDGGraph(context, new PDGActionNode(node, "T",
                null, ASTNode.EMPTY_STATEMENT, null, null, "empty"));
        PDGGraph bg = buildPDG(node, "T", astNode.getBody());
        if (!bg.isEmpty())
            ebg.mergeSequential(bg);
        PDGGraph eg = new PDGGraph(context, new PDGActionNode(node, "F",
                null, ASTNode.EMPTY_STATEMENT, null, null, "empty"));
        pdg.mergeBranches(ebg, eg);
        /*
         * pdg.sinks.remove(node); pdg.statementSinks.remove(node);
         */
        pdg.adjustBreakNodes("");
        context.removeScope();
        return pdg;

    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              VariableDeclarationStatement astNode) {
        PDGGraph pdg = buildPDG(control, branch, (ASTNode) astNode.fragments()
                .get(0));
        for (int i = 1; i < astNode.fragments().size(); i++)
            pdg.mergeSequential(buildPDG(control, branch, (ASTNode) astNode
                    .fragments().get(i)));
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              VariableDeclarationFragment astNode) {
        SimpleName name = astNode.getName();
        String type = JavaASTUtil.getSimpleType(astNode);
        context.addLocalVariable(name.getIdentifier(), "" + name.getStartPosition(), type);
        PDGDataNode node = new PDGDataNode(name, name.getNodeType(),
                "" + name.getStartPosition(), type,
                name.getIdentifier(), false, true);
        if (astNode.getInitializer() == null) {
            PDGGraph pdg = new PDGGraph(context, new PDGDataNode(null, ASTNode.NULL_LITERAL, "null", "", "null"));
            pdg.mergeSequentialData(new PDGActionNode(control, branch,
                    astNode, ASTNode.ASSIGNMENT, null, null, "="), PDGDataEdge.Type.PARAMETER);
            pdg.mergeSequentialData(node, PDGDataEdge.Type.DEFINITION);
            return pdg;
        }
        PDGGraph pdg = buildPDG(control, branch, astNode.getInitializer());
        ArrayList<PDGActionNode> rets = pdg.getReturns();
        if (rets.size() > 0) {
            for (PDGActionNode ret : rets) {
                ret.setAstNodeType(ASTNode.ASSIGNMENT);
                ret.setName("=");
                pdg.extend(ret, new PDGDataNode(node), PDGDataEdge.Type.DEFINITION);
            }

            return pdg;
        }
        ArrayList<PDGDataNode> defs = pdg.getDefinitions();
        if (defs.isEmpty()) {
            pdg.mergeSequentialData(new PDGActionNode(control, branch,
                    astNode, ASTNode.ASSIGNMENT, null, null, "="), PDGDataEdge.Type.PARAMETER);
            pdg.mergeSequentialData(node, PDGDataEdge.Type.DEFINITION);
        } else {
            if (defs.get(0).isDummy()) {
                for (PDGDataNode def : defs) {
                    defStore.remove(def.getKey());
                    def.copyData(node);
                    HashSet<PDGDataNode> ns = defStore.get(def.getKey());
                    if (ns == null) {
                        ns = new HashSet<>();
                        defStore.put(def.getKey(), ns);
                    }
                    ns.add(def);
                }
            } else {
                PDGDataNode def = defs.get(0);
                pdg.mergeSequentialData(
                        new PDGDataNode(def.getAstNode(), def.getAstNodeType(), def.getKey(), def.getDataType(),
                                def.getDataName(), def.isField(), false),
                        PDGDataEdge.Type.REFERENCE);
                pdg.mergeSequentialData(new PDGActionNode(control, branch,
                        astNode, ASTNode.ASSIGNMENT, null, null, "="), PDGDataEdge.Type.PARAMETER);
                pdg.mergeSequentialData(node, PDGDataEdge.Type.DEFINITION);
            }
        }
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              VariableDeclarationExpression astNode) {
        PDGGraph pdg = buildPDG(control, branch, (ASTNode) astNode.fragments()
                .get(0));
        for (int i = 1; i < astNode.fragments().size(); i++)
            pdg.mergeSequential(buildPDG(control, branch, (ASTNode) astNode
                    .fragments().get(i)));
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              TypeLiteral astNode) {
        PDGGraph pdg = new PDGGraph(context, new PDGDataNode(
                astNode, astNode.getNodeType(), "class", JavaASTUtil.getSimpleType(astNode.getType()), "class"));
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              TryStatement astNode) {
        if (astNode.getBody().statements().isEmpty())
            return new PDGGraph(context);
        context.pushTry();
        PDGGraph pdg = new PDGGraph(context);
        PDGControlNode node = new PDGControlNode(control, branch,
                astNode, astNode.getNodeType());
        pdg.mergeSequentialControl(node, "");
        PDGGraph[] gs = new PDGGraph[astNode.catchClauses().size() + 1];
        gs[0] = buildPDG(node, "T", astNode.getBody());
        for (int i = 0; i < astNode.catchClauses().size(); i++) {
            CatchClause cc = (CatchClause) astNode.catchClauses().get(i);
            gs[i + 1] = buildPDG(node, "F", cc);
        }
        pdg.mergeBranches(gs);
        // TODO
        // astNode.getFinally();
        context.popTry();
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              ThrowStatement astNode) {
        PDGGraph pdg = buildArgumentPDG(control, branch, astNode.getExpression());
        PDGActionNode node = new PDGActionNode(control, branch,
                astNode, astNode.getNodeType(), null, null, "throw");
        pdg.mergeSequentialData(node, PDGDataEdge.Type.PARAMETER);
        pdg.returns.add(node);
        pdg.sinks.remove(node);
        pdg.statementSinks.remove(node);
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              SynchronizedStatement astNode) {
        PDGGraph pdg = buildPDG(control, branch, astNode.getExpression());
        PDGControlNode node = new PDGControlNode(control, branch,
                astNode, astNode.getNodeType());
        pdg.mergeSequentialData(node, PDGDataEdge.Type.PARAMETER);
        if (!astNode.getBody().statements().isEmpty()) {
            pdg.mergeSequentialControl(new PDGActionNode(node, "",
                    null, ASTNode.EMPTY_STATEMENT, null, null, "empty"), "");
            pdg.mergeSequential(buildPDG(node, "", astNode.getBody()));
            return pdg;
        }
        return new PDGGraph(context);
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              SwitchStatement astNode) {
        // TODO
        if (true) return new PDGGraph(context);
        if (astNode.statements().size() > 100)
            return new PDGGraph(context);
        PDGControlNode snode = new PDGControlNode(control, branch,
                astNode, astNode.getNodeType());
        PDGGraph pdg = new PDGGraph(context, snode);
        PDGGraph ebg = new PDGGraph(context, new PDGActionNode(snode, "T",
                null, ASTNode.EMPTY_STATEMENT, null, null, "empty"));
        java.util.List<?> statements = astNode.statements();
        int s = 0;
        while (s < statements.size()) {
            if (statements.get(s) instanceof SwitchCase)
                break;
        }
        for (int e = s + 1; e < statements.size(); e++) {
            if (statements.get(e) instanceof SwitchCase || e == statements.size() - 1) {
                if (!(statements.get(e) instanceof SwitchCase))
                    e = statements.size();
                if (e > s + 1) {
                    SwitchCase sc = (SwitchCase) statements.get(s);
                    PDGGraph cg = null;
                    if (sc.isDefault()) {
                        cg = buildPDG(snode, "T", statements.subList(s+1, e));
                        ebg.mergeSequential(cg);
                    } else {
                        PDGActionNode ccnode = new PDGActionNode(snode, "T", null, ASTNode.INFIX_EXPRESSION, null, null, "==");
                        PDGGraph exg = buildArgumentPDG(snode, "T", astNode.getExpression());
                        exg.mergeSequentialData(ccnode, PDGDataEdge.Type.PARAMETER);
                        PDGGraph cexg = buildArgumentPDG(snode, "T", ((SwitchCase) statements.get(s)).getExpression());
                        cexg.mergeSequentialData(ccnode, PDGDataEdge.Type.PARAMETER);
                        cg = new PDGGraph(context);
                        cg.mergeParallel(exg, cexg);
                        cg.mergeSequentialData(new PDGActionNode(snode, "T", null, ASTNode.ASSIGNMENT, null, null, "="), PDGDataEdge.Type.PARAMETER);
                        PDGDataNode dummy = new PDGDataNode(null, ASTNode.SIMPLE_NAME,
                                PDGNode.PREFIX_DUMMY + astNode.getStartPosition() + "_"
                                        + astNode.getLength(), "boolean", PDGNode.PREFIX_DUMMY, false, true);
                        cg.mergeSequentialData(dummy, PDGDataEdge.Type.DEFINITION);
                        cg.mergeSequentialData(new PDGDataNode(dummy.getAstNode(), dummy.getAstNodeType(), dummy.getKey(), dummy.getDataType(), dummy.getDataName()), PDGDataEdge.Type.REFERENCE);

                        PDGControlNode cnode = new PDGControlNode(snode, "T", sc, ASTNode.IF_STATEMENT);
                        cg.mergeSequentialData(cnode, PDGDataEdge.Type.CONDITION);

                        PDGGraph etg = new PDGGraph(context, new PDGActionNode(cnode, "T",
                                null, ASTNode.EMPTY_STATEMENT, null, null, "empty"));
                        PDGGraph tg = buildPDG(cnode, "T", statements.subList(s+1, e));
                        if (!tg.isEmpty()) {
                            etg.mergeSequential(tg);
                            PDGGraph efg = new PDGGraph(context, new PDGActionNode(cnode, "F",
                                    null, ASTNode.EMPTY_STATEMENT, null, null, "empty"));
                            cg.mergeBranches(etg, efg);
                            ebg.mergeSequential(cg);
                        }
                    }
                }
                s = e;
            }
        }
        PDGGraph eg = new PDGGraph(context, new PDGActionNode(snode, "F",
                null, ASTNode.EMPTY_STATEMENT, null, null, "empty"));
        pdg.mergeBranches(ebg, eg);
        /*
         * pdg.sinks.remove(node); pdg.statementSinks.remove(node);
         */
        pdg.adjustBreakNodes("");
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              SuperMethodInvocation astNode) {
        PDGGraph[] pgs = new PDGGraph[astNode.arguments().size() + 1];
        pgs[0] = new PDGGraph(context, new PDGDataNode(
                null, ASTNode.THIS_EXPRESSION, "this",
                "super", "super"));
        for (int i = 0; i < astNode.arguments().size(); i++)
            pgs[i+1] = buildArgumentPDG(control, branch,
                    (Expression) astNode.arguments().get(i));
        PDGActionNode node = new PDGActionNode(control, branch,
                astNode, astNode.getNodeType(), null, "super." + astNode.getName().getIdentifier() + "()",
                astNode.getName().getIdentifier());
        PDGGraph pdg = null;
        pgs[0].mergeSequentialData(node, PDGDataEdge.Type.RECEIVER);
        if (pgs.length > 0) {
            for (int i = 1; i < pgs.length; i++)
                pgs[i].mergeSequentialData(node, PDGDataEdge.Type.PARAMETER);
            pdg = new PDGGraph(context);
            pdg.mergeParallel(pgs);
        } else
            pdg = new PDGGraph(context, node);
        // skip astNode.getQualifier()
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              PyTupleExpression astNode) {
        PDGDataNode dummy = new PDGDataNode(null, ASTNode.SIMPLE_NAME,
                PDGNode.PREFIX_DUMMY + astNode.getStartPosition() + "_"
                        + astNode.getLength(), "boolean", PDGNode.PREFIX_DUMMY, false, true);
        int size_filterd_array = ((java.util.List)astNode.expressions().stream().filter(x -> !(x instanceof SimpleName && ((SimpleName) x).getIdentifier().equals("PyCpatDummy"))).collect(Collectors.toList())).size();
        PDGGraph[] pgs = new PDGGraph[size_filterd_array];
        if (astNode.expressions().size() <= 10) {
            for (int i = 0; i < astNode.expressions().size(); i++){
                if (astNode.expressions().get(i) instanceof SimpleName && ((SimpleName) astNode.expressions().get(i)).getIdentifier().equals("PyCpatDummy"))
                    continue;
                pgs[i] = buildArgumentPDG(control, branch, (Expression) astNode.expressions().get(i));
            }

        } else
            pgs = new PDGGraph[0];
        if (Arrays.stream(pgs).sequential().filter(x->x!=null).collect(Collectors.toList()).size()==0){
            pgs = new PDGGraph[0];
        }

        PDGNode node = new PDGActionNode(control, branch, astNode, astNode.getNodeType(), null, "()", "()");

        if (pgs.length > 0) {
            for (PDGGraph pg : pgs) {
                if (pg != null)
                    pg.mergeSequentialData(node, PDGDataEdge.Type.PARAMETER);
            }
            PDGGraph pdg = new PDGGraph(context);
            pdg.mergeParallel(pgs);
            if (astNode.getProperty("Assignment")=="Assignment"){
                pdg.mergeSequentialData(dummy, PDGDataEdge.Type.DEFINITION);
            }
            return pdg;
        } else{
            PDGGraph pdg = new PDGGraph(context, node);
            if (astNode.getProperty("Assignment")=="Assignment"){
                pdg.mergeSequentialData(dummy, PDGDataEdge.Type.DEFINITION);
            }
//			pdg.mergeSequentialData(dummy,Type.DEFINITION);
            return pdg;
        }

    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              SuperConstructorInvocation astNode) {
        PDGGraph[] pgs = new PDGGraph[astNode.arguments().size()];
        for (int i = 0; i < astNode.arguments().size(); i++) {
            pgs[i] = buildArgumentPDG(control, branch,
                    (Expression) astNode.arguments().get(i));
        }
        PDGActionNode node = new PDGActionNode(control, branch,
                astNode, astNode.getNodeType(), null, "super()", "<new>");
        PDGGraph pdg = null;
        if (pgs.length > 0) {
            for (PDGGraph pg : pgs)
                pg.mergeSequentialData(node, PDGDataEdge.Type.PARAMETER);
            pdg = new PDGGraph(context);
            pdg.mergeParallel(pgs);
        } else
            pdg = new PDGGraph(context, node);
        // skip astNode.getExpression()
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              StringLiteral astNode) {
        String lit = "";
        try {
            lit = astNode.getLiteralValue();
        } catch (IllegalArgumentException e) {}
        PDGGraph pdg = new PDGGraph(context, new PDGDataNode(
                astNode, astNode.getNodeType(), astNode.getEscapedValue(), "String",
                lit));
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              SingleVariableDeclaration astNode) {
        SimpleName name = astNode.getName();
        String type = JavaASTUtil.getSimpleType(astNode.getType());
        context.addLocalVariable(name.getIdentifier(), "" + name.getStartPosition(), type);
        PDGDataNode node = new PDGDataNode(name, name.getNodeType(),
                "" + name.getStartPosition(), type,
                name.getIdentifier(), false, true);
        PDGGraph pdg = new PDGGraph(context, new PDGDataNode(null, ASTNode.NULL_LITERAL, "null", "", "null"));
        pdg.mergeSequentialData(new PDGActionNode(control, branch,
                astNode, ASTNode.ASSIGNMENT, null, null, "="), PDGDataEdge.Type.PARAMETER);
        pdg.mergeSequentialData(node, PDGDataEdge.Type.DEFINITION);
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch, SimpleName astNode) {
        String name = astNode.getIdentifier();
        String[] info = context.getLocalVariableInfo(name);
        if (info != null) {
            PDGGraph pdg = new PDGGraph(context, new PDGDataNode(
                    astNode, astNode.getNodeType(), info[0], info[1],
                    astNode.getIdentifier(), false, false));
            return pdg;
        }
        String type = context.getFieldType(name);
        if (type != null) {
            PDGGraph pdg = new PDGGraph(context, new PDGDataNode(
                    null, ASTNode.THIS_EXPRESSION, "this",
                    "this", "this"));
            pdg.mergeSequentialData(new PDGDataNode(astNode, ASTNode.FIELD_ACCESS,
                    "this." + name, type, name, true,
                    false), PDGDataEdge.Type.QUALIFIER);
            return pdg;
        }
        if (Character.isUpperCase(name.charAt(0))) {
            PDGGraph pdg = new PDGGraph(context, new PDGDataNode(
                    astNode, astNode.getNodeType(), name, name,
                    name, false, false));
            return pdg;
        }
        else if(context.getImportsMap().containsKey(name)){
            PDGGraph pdg = new PDGGraph(context, new PDGDataNode(
                    astNode, astNode.getNodeType(), name, name,
                    name, false, false));
            return pdg;
        }
        PDGGraph pdg = new PDGGraph(context, new PDGDataNode(
                null, ASTNode.THIS_EXPRESSION, "this",
                "this", "this"));
        pdg.mergeSequentialData(new PDGDataNode(astNode, ASTNode.FIELD_ACCESS,
                "this." + name, "UNKNOWN", name, true,
                false), PDGDataEdge.Type.QUALIFIER);
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              ReturnStatement astNode) {
        PDGGraph pdg = null;
        PDGActionNode node = null;
        if (astNode.getExpression() != null) {
            pdg = buildArgumentPDG(control, branch, astNode.getExpression());
            node = new PDGActionNode(control, branch, astNode, astNode.getNodeType(),
                    null, null, "return");
            pdg.mergeSequentialData(node, PDGDataEdge.Type.PARAMETER);
        } else {
            node = new PDGActionNode(control, branch, astNode, astNode.getNodeType(),
                    null, null, "return");
            pdg = new PDGGraph(context, node);
        }
        pdg.returns.add(node);
        pdg.sinks.clear();
        pdg.statementSinks.clear();
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              PyYieldReturnStatement astNode){
        PDGGraph pdg = null;
        PDGActionNode node = null;
        if (astNode.getExpression()!=null){
            pdg = buildArgumentPDG(control, branch, astNode.getExpression());
            node = new PDGActionNode(control, branch, astNode, astNode.getNodeType(),
                    null, null, "yield");
            pdg.mergeSequentialData(node, PDGDataEdge.Type.PARAMETER);
        }
        else{
            node = new PDGActionNode(control, branch, astNode, astNode.getNodeType(),
                    null, null, "yield");
            pdg = new PDGGraph(context, node);
        }
//		pdg.returns.add(node);
//		pdg.sinks.remove(node);
//		pdg.statementSinks.remove(node);
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              PyNonLocalStatement astNode){
        PDGGraph pdg = null;
        PDGActionNode node = null;
        if (astNode.getExpression()!=null){
            pdg = buildArgumentPDG(control, branch, astNode.getExpression());
            node = new PDGActionNode(control, branch, astNode, astNode.getNodeType(),
                    null, null, "nonlocal");
            pdg.mergeSequentialData(node, PDGDataEdge.Type.PARAMETER);
        }
        else{
            node = new PDGActionNode(control, branch, astNode, astNode.getNodeType(),
                    null, null, "nonlocal");
            pdg = new PDGGraph(context, node);
        }
        pdg.returns.add(node);
        pdg.sinks.remove(node);
        pdg.statementSinks.remove(node);
        return pdg;
    }


    private PDGGraph buildPDG(PDGNode control, String branch,
                              QualifiedName astNode) {
        PDGGraph pdg = buildArgumentPDG(control, branch, astNode.getQualifier());
        PDGDataNode node = pdg.getOnlyDataOut();
        if (node.getDataType().startsWith("UNKNOWN")) {
            String name = astNode.getName().getIdentifier();
            if (Character.isUpperCase(name.charAt(0))) {
                return new PDGGraph(context, new PDGDataNode(astNode, ASTNode.FIELD_ACCESS, astNode.getFullyQualifiedName(),
                        astNode.getFullyQualifiedName(), astNode.getName().getIdentifier(), true, false));
            }
        } else
            pdg.mergeSequentialData(
                    new PDGDataNode(astNode, ASTNode.FIELD_ACCESS, astNode.getFullyQualifiedName(),
                            node.getDataType() + "." + astNode.getName().getIdentifier(),
                            astNode.getName().getIdentifier(), true, false), PDGDataEdge.Type.QUALIFIER);
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              PrefixExpression astNode) {
        PDGGraph pdg = buildArgumentPDG(control, branch, astNode.getOperand());
        PDGDataNode node = pdg.getOnlyDataOut();
        if (astNode.getOperator() == PrefixExpression.Operator.PLUS)
            return pdg;
        if (astNode.getOperator() == PrefixExpression.Operator.INCREMENT
                || astNode.getOperator() == PrefixExpression.Operator.DECREMENT) {
            PDGGraph rg = new PDGGraph(context, new PDGDataNode(
                    null, ASTNode.NUMBER_LITERAL, "1", node.getDataType(), "1"));
            PDGActionNode op = new PDGActionNode(control, branch,
                    astNode, astNode.getNodeType(), null, null,
                    astNode.getOperator().toString().substring(0, 1));
            pdg.mergeSequentialData(op, PDGDataEdge.Type.PARAMETER);
            rg.mergeSequentialData(op, PDGDataEdge.Type.PARAMETER);
            pdg.mergeParallel(rg);
        } else
            pdg.mergeSequentialData(
                    new PDGActionNode(control, branch, astNode, astNode.getNodeType(),
                            null, null, astNode.getOperator().toString()),
                    PDGDataEdge.Type.PARAMETER);
        if (astNode.getOperator() == PrefixExpression.Operator.INCREMENT
                || astNode.getOperator() == PrefixExpression.Operator.DECREMENT) {
            pdg.mergeSequentialData(new PDGActionNode(control, branch,
                    astNode, ASTNode.ASSIGNMENT, null, null, "="), PDGDataEdge.Type.PARAMETER);
            pdg.mergeSequentialData(new PDGDataNode(node.getAstNode(), node.getAstNodeType(), node.getKey(),
                    node.getDataType(), node.getDataName()), PDGDataEdge.Type.DEFINITION);
        }
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              PostfixExpression astNode) {
        PDGGraph lg = buildArgumentPDG(control, branch, astNode.getOperand());
        PDGDataNode node = lg.getOnlyDataOut();
        // FIXME handling postfix expression more precisely
        PDGGraph rg = new PDGGraph(context, new PDGDataNode(
                null, ASTNode.NUMBER_LITERAL, "1", node.getDataType(), "1"));
        PDGActionNode op = new PDGActionNode(control, branch,
                astNode, astNode.getNodeType(), null, null, astNode.getOperator().toString()
                .substring(0, 1));
        lg.mergeSequentialData(op, PDGDataEdge.Type.PARAMETER);
        rg.mergeSequentialData(op, PDGDataEdge.Type.PARAMETER);
        PDGGraph pdg = new PDGGraph(context);
        pdg.mergeParallel(lg, rg);
        pdg.mergeSequentialData(new PDGActionNode(control, branch,
                astNode, ASTNode.ASSIGNMENT, null, null, "="), PDGDataEdge.Type.PARAMETER);
        pdg.mergeSequentialData(new PDGDataNode(node.getAstNode(), node.getAstNodeType(), node.getKey(),
                node.getDataType(), node.getDataName()), PDGDataEdge.Type.DEFINITION);
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              ParenthesizedExpression astNode) {
        return buildPDG(control, branch, astNode.getExpression());
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              NumberLiteral astNode) {
        PDGGraph pdg = new PDGGraph(context, new PDGDataNode(
                astNode, astNode.getNodeType(), astNode.getToken(), "number",
                astNode.getToken()));
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              NullLiteral astNode) {
        PDGGraph pdg = new PDGGraph(context, new PDGDataNode(
                astNode, astNode.getNodeType(), astNode.toString(), "null",
                astNode.toString()));
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              MethodInvocation astNode) {
        if (astNode.getName().getIdentifier().toLowerCase().contains("assert"))
            return new PDGGraph(context, new PDGActionNode(control, branch,
                    astNode, astNode.getNodeType(), null, null, "assert"));
        if (astNode.getName().getIdentifier().equals("exit")
                && astNode.getExpression() != null && astNode.getExpression().toString().equals("System")) {
            PDGActionNode node = new PDGActionNode(control, branch,
                    astNode, astNode.getNodeType(), null, "Sytem.exit()", astNode.getName().getIdentifier());
            PDGGraph pdg = new PDGGraph(context, node);
            pdg.returns.add(node);
            pdg.sinks.remove(node);
            pdg.statementSinks.remove(node);
            return pdg;
        }
        PDGGraph[] pgs = new PDGGraph[astNode.arguments().size() + 1];
        if (astNode.arguments().size() > 100)
            pgs = new PDGGraph[1];
        if (astNode.getExpression() != null)
            pgs[0] = buildArgumentPDG(control, branch,
                    astNode.getExpression());
        else
            pgs[0] = new PDGGraph(context, new PDGDataNode(
                    null, ASTNode.THIS_EXPRESSION, "this",
                    "this", "this"));
        if (astNode.arguments().size() <= 100)
            for (int i = 0; i < astNode.arguments().size(); i++)
                pgs[i + 1] = buildArgumentPDG(control, branch, (Expression) astNode.arguments().get(i));
        PDGActionNode node = new PDGActionNode(control, branch,
                astNode, astNode.getNodeType(), null,
                pgs[0].getOnlyOut().getDataType() + "." + astNode.getName().getIdentifier() + "()",
                astNode.getName().getIdentifier());
        PDGGraph pdg = null;
        pgs[0].mergeSequentialData(node, PDGDataEdge.Type.RECEIVER);
        if (pgs.length > 0) {
            for (int i = 1; i < pgs.length; i++)
                pgs[i].mergeSequentialData(node, PDGDataEdge.Type.PARAMETER);
            pdg = new PDGGraph(context);
            pdg.mergeParallel(pgs);
        } else
            pdg = new PDGGraph(context, node);
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              MethodDeclaration astNode) {
        PDGGraph pdg = new PDGGraph(context);
        // skip
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              LabeledStatement astNode) {
        adjustBreakNodes(astNode.getLabel().getIdentifier());
        return buildPDG(control, branch, astNode.getBody());
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              InstanceofExpression astNode) {
        PDGGraph pdg = buildArgumentPDG(control, branch,
                astNode.getLeftOperand());
        PDGNode node = new PDGActionNode(control, branch,
                astNode, astNode.getNodeType(), null, null,
                JavaASTUtil.getSimpleType(astNode.getRightOperand()) + ".<instanceof>");
        pdg.mergeSequentialData(node, PDGDataEdge.Type.PARAMETER);
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              Initializer astNode) {
        return buildPDG(control, branch, astNode.getBody());
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              PyInExpression astNode){
        PDGGraph pdg = new PDGGraph(context);

        PDGGraph lg = buildArgumentPDG(control, branch,
                astNode.getLeftOperand());
        PDGGraph rg = buildArgumentPDG(control, branch,
                astNode.getRightOperand());
        PDGActionNode node = new PDGActionNode(control, branch,
                astNode, astNode.getNodeType(), null, null, "in");

        lg.mergeSequentialData(node, PDGDataEdge.Type.PARAMETER);
        rg.mergeSequentialData(node, PDGDataEdge.Type.PARAMETER);
        pdg.mergeParallel(lg, rg);
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              PyNotInExpression astNode){
        PDGGraph pdg = new PDGGraph(context);

        PDGGraph lg = buildArgumentPDG(control, branch,
                astNode.getLeftOperand());
        PDGGraph rg = buildArgumentPDG(control, branch,
                astNode.getRightOperand());
        PDGActionNode node = new PDGActionNode(control, branch,
                astNode, astNode.getNodeType(), null, null, "not in");

        lg.mergeSequentialData(node, PDGDataEdge.Type.PARAMETER);
        rg.mergeSequentialData(node, PDGDataEdge.Type.PARAMETER);
        pdg.mergeParallel(lg, rg);
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              InfixExpression astNode) {
        PDGGraph pdg = new PDGGraph(context);
        java.util.List l = astNode.extendedOperands();
        if (l != null && l.size() > 10 - 2)
            return new PDGGraph(context, new PDGActionNode(control, branch, astNode, astNode.getNodeType(), null, null, astNode.getOperator().toString()));
        PDGGraph lg = buildArgumentPDG(control, branch,
                astNode.getLeftOperand());
        PDGGraph rg = buildArgumentPDG(control, branch,
                astNode.getRightOperand());
        PDGActionNode node = new PDGActionNode(control, branch,
                astNode, astNode.getNodeType(), null, null, astNode.getOperator().toString());
        lg.mergeSequentialData(node, PDGDataEdge.Type.PARAMETER);
        rg.mergeSequentialData(node, PDGDataEdge.Type.PARAMETER);
        pdg.mergeParallel(lg, rg);
        if (astNode.hasExtendedOperands())
            for (int i = 0; i < astNode.extendedOperands().size(); i++) {
                PDGGraph tmp = buildArgumentPDG(control, branch,
                        (Expression) astNode.extendedOperands().get(i));
                tmp.mergeSequentialData(node, PDGDataEdge.Type.PARAMETER);
                pdg.mergeParallel(tmp);
            }
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              IfStatement astNode) {
        context.addScope();
        PDGGraph pdg = buildArgumentPDG(control, branch,
                astNode.getExpression());
        PDGControlNode node = new PDGControlNode(control, branch,
                astNode, astNode.getNodeType());
        pdg.mergeSequentialData(node, PDGDataEdge.Type.CONDITION);
        PDGGraph etg = new PDGGraph(context, new PDGActionNode(node, "T",
                null, ASTNode.EMPTY_STATEMENT, null, null, "empty"));
        PDGGraph tg = buildPDG(node, "T", astNode.getThenStatement());
        if (!tg.isEmpty())
            etg.mergeSequential(tg);
        PDGGraph efg = new PDGGraph(context, new PDGActionNode(node, "F",
                null, ASTNode.EMPTY_STATEMENT, null, null, "empty"));
        if (astNode.getElseStatement() != null) {
            PDGGraph fg = buildPDG(node, "F", astNode.getElseStatement());
            if (!fg.isEmpty())
                efg.mergeSequential(fg);
        }
        pdg.mergeBranches(etg, efg);
        /*
         * pdg.sinks.remove(node); pdg.statementSinks.remove(node);
         */
        context.removeScope();
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              ForStatement astNode) {
        context.addScope();
        PDGGraph pdg = null;
        if (astNode.initializers() != null && astNode.initializers().size() > 0) {
            pdg = buildPDG(control, branch, (ASTNode) astNode.initializers()
                    .get(0));
            for (int i = 1; i < astNode.initializers().size(); i++)
                pdg.mergeSequential(buildPDG(control, branch, (ASTNode) astNode
                        .initializers().get(i)));
        }
        PDGGraph middleG = null;
        if (astNode.getExpression() != null) {
            middleG = buildArgumentPDG(control, branch, astNode.getExpression());
        }
        PDGControlNode node = new PDGControlNode(control, branch,
                astNode, astNode.getNodeType());
        if (middleG != null)
            middleG.mergeSequentialData(node, PDGDataEdge.Type.CONDITION);
        else
            middleG = new PDGGraph(context, node);
        PDGGraph ebg = new PDGGraph(context, new PDGActionNode(node, "T", null, ASTNode.EMPTY_STATEMENT, null, null, "empty"));
        PDGGraph bg = buildPDG(node, "T", astNode.getBody());
        if (!bg.isEmpty()) {
            ebg.mergeSequential(bg);
        }
        if (astNode.updaters() != null && astNode.updaters().size() > 0) {
            PDGGraph ug = buildPDG(node, "T", (ASTNode) astNode.updaters()
                    .get(0));
            for (int i = 1; i < astNode.updaters().size(); i++) {
                ug.mergeSequential(buildPDG(node, "T", (ASTNode) astNode
                        .updaters().get(i)));
            }
            ebg.mergeSequential(ug);
        }

        PDGGraph eg = new PDGGraph(context, new PDGActionNode(node, "F", null, ASTNode.EMPTY_STATEMENT, null, null, "empty"));
        middleG.mergeBranches(ebg, eg);
        if (pdg == null)
            pdg = middleG;
        else
            pdg.mergeSequential(middleG);
        /*
         * pdg.sinks.remove(node); pdg.statementSinks.remove(node);
         */
        pdg.adjustBreakNodes("");
        context.removeScope();
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              ExpressionStatement astNode) {
        PDGGraph pdg = buildPDG(control, branch, astNode.getExpression());
        ArrayList<PDGActionNode> rets = pdg.getReturns();
        if (rets.size() > 0) {
            for (PDGNode ret : new HashSet<PDGNode>(rets)) {
                for (PDGEdge e : new HashSet<PDGEdge>(ret.getInEdges()))
                    if (e.getSource() instanceof PDGDataNode)
                        pdg.delete(e.getSource());
                pdg.delete(ret);
            }
        }
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              ConditionalExpression astNode) {
        PDGDataNode dummy = new PDGDataNode(null, ASTNode.SIMPLE_NAME,
                PDGNode.PREFIX_DUMMY + astNode.getStartPosition() + "_"
                        + astNode.getLength(), "boolean", PDGNode.PREFIX_DUMMY, false, true);

        PDGGraph pdg = buildArgumentPDG(control, branch,
                astNode.getExpression());

        PDGControlNode node = new PDGControlNode(control, branch,
                astNode, ASTNode.IF_STATEMENT);
        pdg.mergeSequentialData(node, PDGDataEdge.Type.CONDITION);

        PDGGraph tg = buildArgumentPDG(node, "T", astNode.getThenExpression());
        tg.mergeSequentialData(new PDGActionNode(node, "T", null, ASTNode.ASSIGNMENT,
                null, null, "="), PDGDataEdge.Type.PARAMETER);
        tg.mergeSequentialData(new PDGDataNode(dummy), PDGDataEdge.Type.DEFINITION);
        PDGGraph fg = buildArgumentPDG(node, "F", astNode.getElseExpression());
        fg.mergeSequentialData(new PDGActionNode(node, "F", null, ASTNode.ASSIGNMENT,
                null, null, "="), PDGDataEdge.Type.PARAMETER);
        fg.mergeSequentialData(new PDGDataNode(dummy), PDGDataEdge.Type.DEFINITION);
        pdg.mergeBranches(tg, fg);
        /*
         * pdg.sinks.remove(node); pdg.statementSinks.remove(node);
         */
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              PyGenerator astNode){
        context.addScope();
        PDGGraph[] pgs = new PDGGraph[astNode.getComparators().size()];
        int numOfComparators = 0;
        for (Object comparator : astNode.getComparators()) {
            PyComparator comp = (PyComparator) comparator;
            comp.setProperty("TARGET",astNode.getTargetExpression());
            pgs[numOfComparators] = buildArgumentPDG(control, branch,comp);
            numOfComparators++;
        }
        PDGControlNode generator = new PDGControlNode(control, branch, astNode, astNode.getNodeType());
//		PDGActionNode node = new PDGActionNode(control, branch,
//				astNode, astNode.getNodeType(), null, "super()", "<new>");
        PDGGraph pdg = null;
        if (pgs.length > 0) {
            for (PDGGraph pg : pgs){
                pg.mergeSequentialData(generator, PDGDataEdge.Type.PARAMETER);
//				pg.mergeSequentialControl(new PDGActionNode(generator, "",
//						null, ASTNode.EMPTY_STATEMENT, null, null, "empty"), "");
            }
            pdg = new PDGGraph(context);
            pdg.mergeParallel(pgs);
        } else{
            pdg = new PDGGraph(context, generator);
        }
        context.removeScope();
        return pdg;
    }

    public PDGGraph buildPDG(PDGNode control, String branch,
                             PyDictComprehension astNode) {
        context.addScope();
        PDGGraph[] pgs = new PDGGraph[astNode.getComparator().size()];
        int numOfComparators = 0;
        for (Object comparator : astNode.getComparator()) {
            PyComparator comp = (PyComparator) comparator;
            comp.setProperty("TARGET",astNode.getTarget1Expression());
            comp.setProperty("TARGET_VALUE",astNode.getTarget2Expression());
            pgs[numOfComparators] = buildArgumentPDG(control, branch,comp);
            numOfComparators++;
        }
        PDGControlNode generator = new PDGControlNode(control, branch, astNode, astNode.getNodeType());

        PDGGraph pdg = null;
        if (pgs.length > 0) {
            for (PDGGraph pg : pgs){
                pg.mergeSequentialData(generator, PDGDataEdge.Type.PARAMETER);
            }
            pdg = new PDGGraph(context);
            pdg.mergeParallel(pgs);
        } else{
            pdg = new PDGGraph(context, generator);
        }
        context.removeScope();
        return pdg;

    }

    public PDGGraph buildPDG(PDGNode control, String branch,
                             PyListComprehension astNode){
        context.addScope();
        PDGGraph[] pgs = new PDGGraph[astNode.getComparator().size()];
        int numOfComparators = 0;
        for (Object comparator : astNode.getComparator()) {
            PyComparator comp = (PyComparator) comparator;
            comp.setProperty("TARGET",astNode.getTargetExpression());
            pgs[numOfComparators] = buildArgumentPDG(control, branch,comp);
            numOfComparators++;
        }
        PDGControlNode generator = new PDGControlNode(control, branch, astNode, astNode.getNodeType());

        PDGGraph pdg = null;
        if (pgs.length > 0) {
            for (PDGGraph pg : pgs){
                pg.mergeSequentialData(generator, PDGDataEdge.Type.PARAMETER);
            }
            pdg = new PDGGraph(context);
            pdg.mergeParallel(pgs);
        } else{
            pdg = new PDGGraph(context, generator);
        }
        context.removeScope();
        return pdg;

    }

//	public PDGGraph buildPDG(PDGNode control, String branch,
//							 PyDictComprehension astNode){
//
//
//	}

    public PDGGraph buildPDG(PDGNode control, String branch,
                             PySetComprehension astNode){
        context.addScope();
        PDGGraph[] pgs = new PDGGraph[astNode.getComparator().size()];
        int numOfComparators = 0;
        for (Object comparator : astNode.getComparator()) {
            PyComparator comp = (PyComparator) comparator;
            comp.setProperty("TARGET",astNode.getTargetExpression());
            pgs[numOfComparators] = buildArgumentPDG(control, branch,comp);
            numOfComparators++;
        }
        PDGControlNode generator = new PDGControlNode(control, branch, astNode, astNode.getNodeType());

        PDGGraph pdg = null;
        if (pgs.length > 0) {
            for (PDGGraph pg : pgs){
                pg.mergeSequentialData(generator, PDGDataEdge.Type.PARAMETER);
//				pg.mergeSequentialControl(new PDGActionNode(generator, "",
//						null, ASTNode.EMPTY_STATEMENT, null, null, "empty"), "");
            }
            pdg = new PDGGraph(context);
            pdg.mergeParallel(pgs);
        } else{
            pdg = new PDGGraph(context, generator);
        }
        context.removeScope();
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              PyComparator astNode){

//		if (true) return new PDGGraph(context);
        java.util.List<PDGDataNode> loopVariables = new ArrayList<>();
        for (Object o : astNode.getValueExpression()) {
            SingleVariableDeclaration var = (SingleVariableDeclaration) o;
            String type = JavaASTUtil.getSimpleType(var.getType());
            SimpleName name = var.getName();
            context.addLocalVariable(name.getIdentifier(), "" + name.getStartPosition(), type);
            PDGDataNode pdn = new PDGDataNode(name, name.getNodeType(), "" + name.getStartPosition(), type,
                    name.getIdentifier(), false, true);
            loopVariables.add(pdn);
        }

        PDGGraph graph = buildArgumentPDG(control, branch, astNode.getIteratorExpression());
        graph.mergeSequentialData(new PDGActionNode(control, branch,
                astNode, ASTNode.ASSIGNMENT, null, null, "="), PDGDataEdge.Type.PARAMETER);

        for (PDGDataNode loopVariable : loopVariables) {
            graph.mergeSequentialData(loopVariable, PDGDataEdge.Type.DEFINITION);
            graph.mergeSequentialData(new PDGDataNode(null, loopVariable.getAstNodeType(),
                    loopVariable.getKey(), loopVariable.getDataType(), loopVariable.getDataName()), PDGDataEdge.Type.REFERENCE);
        }

        if (!(astNode.internalGetConditionalExpression()==null ||
                !(astNode.internalGetConditionalExpression() instanceof SimpleName &&
                        (((SimpleName)astNode.internalGetConditionalExpression()).getIdentifier().equals("DUMMY")
                                ||
                                ((SimpleName)astNode.internalGetConditionalExpression()).getIdentifier().equals("DUMMY_IF")
                        )
                ))){
            PDGDataNode dummy = new PDGDataNode(null, ASTNode.SIMPLE_NAME,
                    PDGNode.PREFIX_DUMMY + astNode.getStartPosition() + "_"
                            + astNode.getLength(), "boolean", PDGNode.PREFIX_DUMMY, false, true);

            PDGGraph pdg = buildArgumentPDG(control, branch,
                    astNode.getConditionalExpression());
            PDGControlNode node = new PDGControlNode(control, branch,
                    astNode, ASTNode.IF_STATEMENT);
            pdg.mergeSequentialData(node, PDGDataEdge.Type.CONDITION);
            PDGGraph tg = buildArgumentPDG(node, "T", (ASTNode) astNode.getProperty("TARGET"));
            tg.mergeSequentialData(new PDGActionNode(node, "T", null, ASTNode.ASSIGNMENT,
                    null, null, "="), PDGDataEdge.Type.PARAMETER);
            if (astNode.getProperty("TARGET_VALUE")!=null){
                PDGGraph tg1 = buildArgumentPDG(node, "T", (ASTNode) astNode.getProperty("TARGET_VALUE"));
                tg1.mergeSequentialData(new PDGActionNode(node, "T", null, ASTNode.ASSIGNMENT,
                        null, null, "="), PDGDataEdge.Type.PARAMETER);
                pdg.mergeBranches(tg,tg1);
                graph.mergeBranches(pdg);

            }
            else{
                pdg.mergeBranches(tg);
                graph.mergeBranches(pdg);
            }


//					tg.mergeSequentialData(new PDGDataNode(dummy), Type.DEFINITION);

        }
        PDGControlNode node = new PDGControlNode(control, branch, astNode, ASTNode.PY_COMPARATOR);
        graph.mergeSequentialData(node, PDGDataEdge.Type.CONDITION);
        graph.mergeSequentialControl(new PDGActionNode(node, "",
                null, ASTNode.EMPTY_STATEMENT, null, null, "empty"), "");
        graph.adjustBreakNodes("");

        return graph;
//			}
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              EnhancedForStatementWithElse astNode){
        context.addScope();
        SimpleName namep = astNode.getParameter().getName();
        String typep = JavaASTUtil.getSimpleType(astNode.getParameter().getType());
        context.addLocalVariable(namep.getIdentifier(), "" + namep.getStartPosition(), typep);
        PDGDataNode varp = new PDGDataNode(namep, namep.getNodeType(),
                "" + namep.getStartPosition(), typep,
                namep.getIdentifier(), false, true);

        java.util.List<PDGDataNode> loopVariables = new ArrayList<>();

        for (Object parameter : astNode.Parameters()) {
            SimpleName name = ((SingleVariableDeclaration)parameter).getName();
            String type = JavaASTUtil.getSimpleType(((SingleVariableDeclaration)parameter).getType());
            context.addLocalVariable(name.getIdentifier(), "" + name.getStartPosition(), type);
            PDGDataNode var = new PDGDataNode(name, name.getNodeType(),
                    "" + name.getStartPosition(), type,
                    name.getIdentifier(), false, true);
            loopVariables.add(var);
        }
        PDGGraph pdg = buildArgumentPDG(control, branch, astNode.getExpression());
        pdg.mergeSequentialData(new PDGActionNode(control, branch,
                astNode, ASTNode.ASSIGNMENT, null, null, "="), PDGDataEdge.Type.PARAMETER);

        pdg.mergeSequentialData(varp, PDGDataEdge.Type.DEFINITION);
        pdg.mergeSequentialData(new PDGDataNode(null, varp.getAstNodeType(),
                varp.getKey(), varp.getDataType(), varp.getDataName()), PDGDataEdge.Type.REFERENCE);

        for (PDGDataNode loopVariable : loopVariables) {
            pdg.mergeSequentialData(loopVariable, PDGDataEdge.Type.DEFINITION);
            pdg.mergeSequentialData(new PDGDataNode(null, loopVariable.getAstNodeType(),
                    loopVariable.getKey(), loopVariable.getDataType(), loopVariable.getDataName()), PDGDataEdge.Type.REFERENCE);
        }

        PDGControlNode node = new PDGControlNode(control, branch, astNode, astNode.getNodeType());
        pdg.mergeSequentialData(node, PDGDataEdge.Type.CONDITION);
        pdg.mergeSequentialControl(new PDGActionNode(node, "",
                null, ASTNode.EMPTY_STATEMENT, null, null, "empty"), "");

        PDGGraph bg = buildPDG(node, "T", astNode.getBody());
        PDGGraph bgelse = buildPDG(node, "F", astNode.getElseBody());
        if (!bg.isEmpty() && bgelse.isEmpty())
            pdg.mergeSequential(bg);
        else if (bg.isEmpty() && !bgelse.isEmpty())
            pdg.mergeSequential(bgelse);
        else
            pdg.mergeParallel(bg,bgelse);
        pdg.adjustBreakNodes("");
        context.removeScope();
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              EnhancedForStatement astNode) {
        context.addScope();
        SimpleName namep = astNode.getParameter().getName();
        String typep = JavaASTUtil.getSimpleType(astNode.getParameter().getType());
        context.addLocalVariable(namep.getIdentifier(), "" + namep.getStartPosition(), typep);
        PDGDataNode varp = new PDGDataNode(namep, namep.getNodeType(),
                "" + namep.getStartPosition(), typep,
                namep.getIdentifier(), false, true);

        java.util.List<PDGDataNode> loopVariables = new ArrayList<>();

        for (Object parameter : astNode.Parameters()) {
            SimpleName name = ((SingleVariableDeclaration)parameter).getName();
            String type = JavaASTUtil.getSimpleType(((SingleVariableDeclaration)parameter).getType());
            context.addLocalVariable(name.getIdentifier(), "" + name.getStartPosition(), type);
            PDGDataNode var = new PDGDataNode(name, name.getNodeType(),
                    "" + name.getStartPosition(), type,
                    name.getIdentifier(), false, true);
            loopVariables.add(var);
        }
        PDGGraph pdg = buildArgumentPDG(control, branch, astNode.getExpression());
        pdg.mergeSequentialData(new PDGActionNode(control, branch,
                astNode, ASTNode.ASSIGNMENT, null, null, "="), PDGDataEdge.Type.PARAMETER);

        pdg.mergeSequentialData(varp, PDGDataEdge.Type.DEFINITION);
        pdg.mergeSequentialData(new PDGDataNode(null, varp.getAstNodeType(),
                varp.getKey(), varp.getDataType(), varp.getDataName()), PDGDataEdge.Type.REFERENCE);

        for (PDGDataNode loopVariable : loopVariables) {
            pdg.mergeSequentialData(loopVariable, PDGDataEdge.Type.DEFINITION);
            pdg.mergeSequentialData(new PDGDataNode(null, loopVariable.getAstNodeType(),
                    loopVariable.getKey(), loopVariable.getDataType(), loopVariable.getDataName()), PDGDataEdge.Type.REFERENCE);
        }

        PDGControlNode node = new PDGControlNode(control, branch, astNode, astNode.getNodeType());
        pdg.mergeSequentialData(node, PDGDataEdge.Type.CONDITION);
        pdg.mergeSequentialControl(new PDGActionNode(node, "",
                null, ASTNode.EMPTY_STATEMENT, null, null, "empty"), "");
        PDGGraph bg = buildPDG(node, "", astNode.getBody());
        if (!bg.isEmpty())
            pdg.mergeSequential(bg);
        pdg.adjustBreakNodes("");
        context.removeScope();
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              DoStatement astNode) {
        context.addScope();
        PDGControlNode node = new PDGControlNode(control, branch,
                astNode, astNode.getNodeType());
        PDGGraph pdg = buildArgumentPDG(control, branch,
                astNode.getExpression());
        pdg.mergeSequentialData(node, PDGDataEdge.Type.CONDITION);
        PDGGraph ebg = new PDGGraph(context, new PDGActionNode(node, "T",
                null, ASTNode.EMPTY_STATEMENT, null, null, "empty"));
        PDGGraph bg = buildPDG(node, "T", astNode.getBody());
        if (!bg.isEmpty())
            ebg.mergeSequential(bg);
        PDGGraph eg = new PDGGraph(context, new PDGActionNode(node, "F",
                null, ASTNode.EMPTY_STATEMENT, null, null, "empty"));
        pdg.mergeBranches(ebg, eg);
        /*
         * pdg.sinks.remove(node); pdg.statementSinks.remove(node);
         */
        pdg.adjustBreakNodes("");
        context.removeScope();
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              ContinueStatement astNode) {
        PDGActionNode node = new PDGActionNode(control, branch,
                astNode, astNode.getNodeType(), astNode.getLabel() == null ? "" : astNode.getLabel().getIdentifier(), null,
                "continue");
        PDGGraph pdg = new PDGGraph(context, node);
        pdg.breaks.add(node);
        pdg.sinks.remove(node);
        pdg.statementSinks.remove(node);
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              ConstructorInvocation astNode) {
        PDGGraph[] pgs = new PDGGraph[astNode.arguments().size()];
        int numOfParameters = 0;
        for (int i = 0; i < astNode.arguments().size(); i++) {
            pgs[numOfParameters] = buildArgumentPDG(control, branch,
                    (Expression) astNode.arguments().get(i));
            numOfParameters++;
        }
        PDGActionNode node = new PDGActionNode(control, branch,
                astNode, astNode.getNodeType(), null, "this()", "<new>");
        PDGGraph pdg = null;
        if (pgs.length > 0) {
            for (PDGGraph pg : pgs)
                pg.mergeSequentialData(node, PDGDataEdge.Type.PARAMETER);
            pdg = new PDGGraph(context);
            pdg.mergeParallel(pgs);
        } else
            pdg = new PDGGraph(context, node);
        return pdg;
    }

//	private PDGGraph buildPDG(PDGNode control, String branch,
//							  PyComparator astNode){
//
//
//	}

    private PDGGraph buildPDG(PDGNode control, String branch,
                              ClassInstanceCreation astNode) {
        PDGGraph[] pgs = new PDGGraph[astNode.arguments().size()];
        int numOfParameters = 0;
        for (int i = 0; i < astNode.arguments().size(); i++) {
            pgs[numOfParameters] = buildArgumentPDG(control, branch,
                    (Expression) astNode.arguments().get(i));
            numOfParameters++;
        }
        PDGActionNode node = new PDGActionNode(control, branch,
                astNode, astNode.getNodeType(), null, JavaASTUtil.getSimpleType(astNode.getType()), "<new>");
        PDGGraph pdg = null;
        if (pgs.length > 0) {
            for (PDGGraph pg : pgs)
                pg.mergeSequentialData(node, PDGDataEdge.Type.PARAMETER);
            pdg = new PDGGraph(context);
            pdg.mergeParallel(pgs);
        } else
            pdg = new PDGGraph(context, node);
        // skip astNode.getExpression()
        // skip astNode.getAnonymousClassDeclaration()
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              CharacterLiteral astNode) {
        PDGGraph pdg = new PDGGraph(context, new PDGDataNode(
                astNode, astNode.getNodeType(), astNode.getEscapedValue(), "char",
                astNode.getEscapedValue()));
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              CatchClause astNode) {
        context.addScope();
        SimpleName name = astNode.getException().getName();
        String type = JavaASTUtil.getSimpleType(astNode.getException().getType());
        context.addLocalVariable(name.getIdentifier(), "" + name.getStartPosition(), type);
        PDGControlNode node = new PDGControlNode(control, branch,
                astNode, astNode.getNodeType());
        PDGGraph pdg = new PDGGraph(context, node);
        PDGGraph cg = new PDGGraph(context, new PDGActionNode(node, "",
                null, ASTNode.EMPTY_STATEMENT, null, null, "empty"));
        if (!astNode.getBody().statements().isEmpty())
            cg.mergeSequential(buildPDG(node, "", astNode.getBody()));
        pdg.mergeSequentialControl(cg);
		/*HashSet<PDGActionNode> nodes = context.getTrys(astNode.getException()
				.getType().resolveBinding());
		for (PDGActionNode n : nodes)
			new PDGDataEdge(n, node, Type.DEPENDENCE);*/
        context.removeScope();
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              CastExpression astNode) {
        PDGGraph pdg = buildArgumentPDG(control, branch,
                astNode.getExpression());
        PDGNode node = new PDGActionNode(control, branch,
                astNode, astNode.getNodeType(), null, JavaASTUtil.getSimpleType(astNode.getType()), JavaASTUtil.getSimpleType(astNode.getType()) + ".<cast>");
        pdg.mergeSequentialData(node, PDGDataEdge.Type.PARAMETER);
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              BreakStatement astNode) {
        PDGActionNode node = new PDGActionNode(control, branch,
                astNode, astNode.getNodeType(), astNode.getLabel() == null ? "" : astNode.getLabel().getIdentifier(), null,
                "break");
        PDGGraph pdg = new PDGGraph(context, node);
        pdg.breaks.add(node);
        pdg.sinks.remove(node);
        pdg.statementSinks.remove(node);
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              BooleanLiteral astNode) {
        PDGGraph pdg = new PDGGraph(context, new PDGDataNode(
                astNode, astNode.getNodeType(), astNode.toString(), "boolean",
                astNode.toString()));
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch, Block astNode) {
        if (astNode.statements().size() > 0 && astNode.statements().size() <= 100) {
            context.addScope();
            PDGGraph pdg = buildPDG(control, branch, astNode.statements());
            context.removeScope();
            return pdg;
        }
        return new PDGGraph(context);
    }

    public PDGGraph buildPDG(PDGNode control, String branch, java.util.List<?> l) {
        ArrayList<PDGGraph> pdgs = new ArrayList<>();
        for (int i = 0; i < l.size(); i++) {
            if (l.get(i) instanceof EmptyStatement) continue;
            PDGGraph pdg = buildPDG(control, branch, (ASTNode) l.get(i));
            if (!pdg.isEmpty())
                pdgs.add(pdg);
        }
        int s = 0;
        for (int i = 1; i < pdgs.size(); i++)
            if (pdgs.get(s).statementNodes.isEmpty())
                s = i;
            else
                pdgs.get(s).mergeSequential(pdgs.get(i));
        if (s == pdgs.size())
            return new PDGGraph(context);
        return pdgs.get(s);
    }

    private void checkParenthesizedNode(ParenthesizedExpression node){
        if (node.getExpression() instanceof ParenthesizedExpression)
            checkParenthesizedNode((ParenthesizedExpression)node.getExpression());
        else if (node.getExpression() instanceof PyTupleExpression){
            node.getExpression().setProperty("Assignment","Assignment");
        }
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              Assignment astNode) {
        if (!(astNode.getLeftHandSide() instanceof Name ||astNode.getLeftHandSide() instanceof PyTupleExpression|| astNode.getLeftHandSide()
                instanceof ArrayAccess||astNode.getLeftHandSide() instanceof ParenthesizedExpression))
            return buildPDG(control, branch, astNode.getRightHandSide());
        if (astNode.getLeftHandSide() instanceof ParenthesizedExpression){
            checkParenthesizedNode((ParenthesizedExpression)astNode.getLeftHandSide());
        }
        else if (astNode.getLeftHandSide()  instanceof PyTupleExpression){
            astNode.getLeftHandSide().setProperty("Assignment","Assignment");
        }

        PDGGraph lg = buildPDG(control, branch, astNode.getLeftHandSide());
        PDGDataNode lnode = lg.getOnlyDataOut();
        PDGGraph pdg = null;
        if (astNode.getOperator() != Assignment.Operator.ASSIGN) {
            String op = JavaASTUtil.getInfixOperator(astNode.getOperator());
            PDGGraph g1 = buildPDG(control, branch, astNode.getLeftHandSide());
            PDGGraph g2 = buildArgumentPDG(control, branch,
                    astNode.getRightHandSide());
            PDGActionNode opNode = new PDGActionNode(control, branch,
                    null, ASTNode.INFIX_EXPRESSION, null, null, op);
            g1.mergeSequentialData(opNode, PDGDataEdge.Type.PARAMETER);
            g2.mergeSequentialData(opNode, PDGDataEdge.Type.PARAMETER);
            pdg = new PDGGraph(context);
            pdg.mergeParallel(g1, g2);
            pdg.mergeSequentialData(new PDGActionNode(control, branch,
                    astNode, ASTNode.ASSIGNMENT, null, null, "="), PDGDataEdge.Type.PARAMETER);
            pdg.mergeSequentialData(lnode, PDGDataEdge.Type.DEFINITION);
        } else {
            pdg = buildPDG(control, branch, astNode.getRightHandSide());
            ArrayList<PDGActionNode> rets = pdg.getReturns();
            if (rets.size() > 0) {
                for (PDGActionNode ret : rets) {
                    ret.setAstNodeType(ASTNode.ASSIGNMENT);
                    ret.setName("=");
                    pdg.extend(ret, new PDGDataNode(lnode), PDGDataEdge.Type.DEFINITION);
                }
                pdg.nodes.addAll(lg.nodes);
                pdg.statementNodes.addAll(lg.statementNodes);
                lg.dataSources.remove(lnode);
                pdg.dataSources.addAll(lg.dataSources);
                pdg.statementSources.addAll(lg.statementSources);
                lg.clear();
                return pdg;
            }
            ArrayList<PDGDataNode> defs = pdg.getDefinitions();
            if (defs.isEmpty()) {
                pdg.mergeSequentialData(new PDGActionNode(control, branch,
                        astNode, ASTNode.ASSIGNMENT, null, null, "="), PDGDataEdge.Type.PARAMETER);
                pdg.mergeSequentialData(lnode, PDGDataEdge.Type.DEFINITION);
            } else {
                if (defs.get(0).isDummy()) {
                    for (PDGDataNode def : defs) {
                        pdg.defStore.remove(def.getKey());
                        def.copyData(lnode);
                        HashSet<PDGDataNode> ns = pdg.defStore.get(def.getKey());
                        if (ns == null) {
                            ns = new HashSet<>();
                            pdg.defStore.put(def.getKey(), ns);
                        }
                        ns.add(def);
                    }
                } else {
                    PDGDataNode def = defs.get(0);
                    pdg.mergeSequentialData(new PDGDataNode(null, def.getAstNodeType(),
                                    def.getKey(), def.getDataType(), def.getDataName()),
                            PDGDataEdge.Type.REFERENCE);
                    pdg.mergeSequentialData(new PDGActionNode(control, branch,
                            astNode, ASTNode.ASSIGNMENT, null, null, "="), PDGDataEdge.Type.PARAMETER);
                    pdg.mergeSequentialData(lnode, PDGDataEdge.Type.DEFINITION);
                }
            }
        }
        pdg.nodes.addAll(lg.nodes);
        pdg.statementNodes.addAll(lg.statementNodes);
        lg.dataSources.remove(lnode);
        pdg.dataSources.addAll(lg.dataSources);
        pdg.statementSources.addAll(lg.statementSources);
        lg.clear();
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              AssertStatement astNode) {
        if (true)
            return new PDGGraph(context, new PDGActionNode(control, branch,
                    astNode, astNode.getNodeType(), null, null, "assert"));
        PDGGraph pdg = buildArgumentPDG(control, branch,
                astNode.getExpression());
        PDGNode node = new PDGActionNode(control, branch,
                astNode, astNode.getNodeType(), null, null, "assert");
        pdg.mergeSequentialData(node, PDGDataEdge.Type.PARAMETER);
        // skip astNode.getMessage()
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              ArrayInitializer astNode) {
        PDGGraph[] pgs = new PDGGraph[astNode.expressions().size()];
        if (astNode.expressions().size() <= 10) {
            for (int i = 0; i < astNode.expressions().size(); i++)
                pgs[i] = buildArgumentPDG(control, branch, (Expression) astNode.expressions().get(i));
        } else
            pgs = new PDGGraph[0];
        PDGNode node = new PDGActionNode(control, branch, astNode, astNode.getNodeType(), null, "{}", "{}");
        if (pgs.length > 0) {
            for (PDGGraph pg : pgs)
                pg.mergeSequentialData(node, PDGDataEdge.Type.PARAMETER);
            PDGGraph pdg = new PDGGraph(context);
            pdg.mergeParallel(pgs);
            return pdg;
        } else
            return new PDGGraph(context, node);
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              ArrayCreation astNode) {
        if (astNode.getInitializer() != null) {
            return buildPDG(control, branch, astNode.getInitializer());
        }
        PDGGraph[] pgs = new PDGGraph[astNode.dimensions().size()];
        if (astNode.dimensions().size() <= 10) {
            for (int i = 0; i < astNode.dimensions().size(); i++)
                pgs[i] = buildArgumentPDG(control, branch, (Expression) astNode.dimensions().get(i));
        } else
            pgs = new PDGGraph[0];
        PDGNode node = new PDGActionNode(control, branch,
                astNode, astNode.getNodeType(), null, "{}", "<new>");
        if (pgs.length > 0) {
            for (PDGGraph pg : pgs)
                pg.mergeSequentialData(node, PDGDataEdge.Type.PARAMETER);
            PDGGraph pdg = new PDGGraph(context);
            pdg.mergeParallel(pgs);
            return pdg;
        } else
            return new PDGGraph(context, node);
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              ArrayAccess astNode) {
        PDGGraph pdg = buildArgumentPDG(control, branch, astNode.getArray());
        String type = pdg.getOnlyOut().getDataType();
        // FIXME type could be null
        if (type != null && type.endsWith("[]"))
            type = type.substring(0, type.length() - 2);
        else
            type = type + "[.]";
        PDGNode node = new PDGDataNode(astNode, astNode.getNodeType(),
                context.getKey(astNode), type,
                astNode.toString());
        pdg.mergeSequentialData(node, PDGDataEdge.Type.QUALIFIER);
        PDGGraph ig = buildArgumentPDG(control, branch, astNode.getIndex());
        ig.mergeSequentialData(node, PDGDataEdge.Type.PARAMETER);
        pdg.mergeBranches(ig);
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              FieldAccess astNode) {
        PDGGraph pdg = buildArgumentPDG(control, branch,
                astNode.getExpression());
        PDGDataNode node = pdg.getOnlyDataOut();
        pdg.mergeSequentialData(
                new PDGDataNode(astNode, astNode.getNodeType(), astNode.toString(),
                        node.getDataType() + "." + astNode.getName().getIdentifier(),
                        astNode.getName().getIdentifier(), true, false), PDGDataEdge.Type.QUALIFIER);
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              SuperFieldAccess astNode) {
        PDGGraph pdg = new PDGGraph(context, new PDGDataNode(
                null, ASTNode.THIS_EXPRESSION, "this", "super", "super"));
        pdg.mergeSequentialData(
                new PDGDataNode(astNode, ASTNode.FIELD_ACCESS, astNode.toString(),
                        "super." + astNode.getName().getIdentifier(),
                        astNode.getName().getIdentifier(), true, false), PDGDataEdge.Type.QUALIFIER);
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              ThisExpression astNode) {
        PDGGraph pdg = new PDGGraph(context, new PDGDataNode(
                astNode, astNode.getNodeType(), "this", "this",
                "this"));
        return pdg;
    }

    private void mergeSequentialControl(PDGNode next, String label) {
        sinks.clear();
        sinks.add(next);
        statementSinks.clear();
        statementSinks.add(next);
        if (statementNodes.isEmpty())
            statementSources.add(next);
        nodes.add(next);
        statementNodes.add(next);
    }


    private void mergeSequentialControl(PDGGraph pdg) {
        if (pdg.statementNodes.isEmpty())
            return;
        if (this.statementNodes.isEmpty() || pdg.statementNodes.isEmpty()) {
            System.err.println("Merge an empty pdg.graph!!!");
            System.exit(-1);
        }
        this.sinks.clear();
        this.statementSinks.clear();
        this.statementSinks.addAll(pdg.statementSinks);
        this.nodes.addAll(pdg.nodes);
        this.statementNodes.addAll(pdg.statementNodes);
        this.breaks.addAll(pdg.breaks);
        this.returns.addAll(pdg.returns);
        pdg.clear();
    }

    private PDGNode getOnlyOut() {
        if (sinks.size() == 1)
            for (PDGNode n : sinks)
                return n;
//        throw new RuntimeException("ERROR in getting the only output node!!!");
//		System.err.println("ERROR in getting the only output node!!!");
//		System.exit(-1);
        return null;
    }

    private void extend(PDGNode ret, PDGDataNode node, PDGDataEdge.Type type) {
        HashSet<PDGDataNode> ns = new HashSet<>();
        ns.add((PDGDataNode) node);
        defStore.put(node.getKey(), ns);
        nodes.add(node);
        sinks.remove(ret);
        sinks.add(node);
        new PDGDataEdge(ret, node, type);
    }

    private void adjustBreakNodes(String id) {
        for (PDGNode node : new HashSet<PDGNode>(breaks)) {
            if ((node.getKey() == null && id == null) || node.getKey().equals(id)) {
                sinks.add(node);
                statementSinks.add(node);
                breaks.remove(node);
            }
        }
    }

    private HashMap<String, HashSet<PDGDataNode>> copyDefStore() {
        HashMap<String, HashSet<PDGDataNode>> store = new HashMap<>();
        for (String key : defStore.keySet())
            store.put(key, new HashSet<>(defStore.get(key)));
        return store;
    }

    private void mergeParallel(PDGGraph... pdgs) {
        HashMap<String, HashSet<PDGDataNode>> defStore = new HashMap<>();
        HashMap<String, Integer> defCounts = new HashMap<>();
        for (PDGGraph pdg : pdgs) {
            HashMap<String, HashSet<PDGDataNode>> localStore = copyDefStore();
            nodes.addAll(pdg.nodes);
            statementNodes.addAll(pdg.statementNodes);
            sinks.addAll(pdg.sinks);
            statementSinks.addAll(pdg.statementSinks);
            dataSources.addAll(pdg.dataSources);
            holeDataSources.addAll(pdg.holeDataSources);
            statementSources.addAll(pdg.statementSources);
            breaks.addAll(pdg.breaks);
            returns.addAll(pdg.returns);
            updateDefStore(localStore, defCounts, pdg.defStore);
            add(defStore, localStore);
            pdg.clear();
        }
        clearDefStore();
        this.defStore = defStore;
    }

    private void updateDefStore(HashMap<String, HashSet<PDGDataNode>> target,
                                HashMap<String, Integer> defCounts,
                                HashMap<String, HashSet<PDGDataNode>> source) {
        for (String key : source.keySet())
            target.put(key, new HashSet<>(source.get(key)));
        for (String key : target.keySet()) {
            int c = 1;
            if (defCounts.containsKey(key))
                c += defCounts.get(key);
            defCounts.put(key, c);
        }
    }

    private PDGDataNode getOnlyDataOut() {
        if (sinks.size() == 1)
            for (PDGNode n : sinks)
                if (n instanceof PDGDataNode)
                    return (PDGDataNode) n;
        System.err.println("ERROR in getting the only data output node!!!" + this.context.getFilePath());

        return null;
    }

    private PDGHoleNode getOnlyHoleDataOut() {
        if (sinks.size() == 1)
            for (PDGNode n : sinks)
                if (n instanceof PDGAlphHole && ((PDGHoleNode) n).isDataNode())
                    return (PDGAlphHole) n;
                else if (n instanceof PDGLazyHole && ((PDGLazyHole) n).isDataNode())
                    return (PDGLazyHole) n;
        System.err.println("ERROR in getting the only data output node!!!" + this.context.getFilePath());
        return null;
    }

    private void delete(PDGNode node) {
        if (statementSinks.contains(node))
            for (PDGEdge e : node.getInEdges())
                if (e instanceof PDGDataEdge) {
                    if (((PDGDataEdge) e).getType() == PDGDataEdge.Type.DEPENDENCE)
                        statementSinks.add(e.getSource());
                    else if (((PDGDataEdge) e).getType() == PARAMETER)
                        sinks.add(e.getSource());
                }
        if (sinks.contains(node) && node instanceof PDGDataNode) {
            for (PDGEdge e : node.getInEdges())
                if (e.getSource() instanceof PDGDataNode)
                    sinks.add(e.getSource());
        }
        if (statementSources.contains(node))
            for (PDGEdge e : node.getOutEdges())
                if (e instanceof PDGDataEdge
                        && ((PDGDataEdge) e).getType() == PDGDataEdge.Type.DEPENDENCE)
                    statementSources.add(e.getTarget());
        nodes.remove(node);
        changedNodes.remove(node);
        statementNodes.remove(node);
        dataSources.remove(node);
        holeDataSources.remove(node);
        statementSources.remove(node);
        sinks.remove(node);
        statementSinks.remove(node);
        node.delete();
    }


//    private PDGGraph buildPDG(PDGNode control, String branch,
//                              AlphanumericHole astNode) {
//
//
//        String name = ":[[l" + (astNode).getInternalN() + "]]";
//        String[] info = context.getLocalVariableInfo(name);
//        if (info != null) {
//            return new PDGGraph(context, new PDGAlphHole(
//                    astNode, astNode.getNodeType(), context.getTypeWrapper().getGuards().getValueOfTemplateVariable(name), info[0], info[1],
//                    name, true, false, false));
//        }
//
//        String type = context.getTypeWrapper().getGuards().getTypeOfTemplateVariable(name);
//        String value = context.getTypeWrapper().getGuards().getValueOfTemplateVariable(name);
//        if (type != null) {
//            context.addLocalVariable(name, "" + astNode.getCharStartIndex(), type);
//            PDGGraph pdg = new PDGGraph(context, new PDGAlphHole(
//                    astNode, astNode.getNodeType(), value, "" + astNode.getCharStartIndex(), type, name, true, false, false));
//
//            return pdg;
//        }
////        if (context.getImportsMap().containsKey(name)) {
////            return new PDGGraph(context, new PDGDataNode(
////                    astNode, astNode.getNodeType(), name, context.getImportsMap().get(name),
////                    name, false, false));
////        } else if (context.getTypeWrapper().getTypeInfo(astNode.getLineno(), astNode.getCol_offset(),astNode.getInternalId()) != null) {
////            return new PDGGraph(context, new PDGDataNode(
////                    astNode, astNode.getNodeType(), name,
////                    context.getTypeWrapper().getTypeInfo(astNode.getLineno(), astNode.getCol_offset(),astNode.getInternalId()),
////                    name, false, false));
////        } else if (Character.isUpperCase(name.charAt(0))) {
////            return new PDGGraph(context, new PDGDataNode(
////                    astNode, astNode.getNodeType(), name, name,
////                    name, false, false));
////        }
////
////        PDGGraph pdg = new PDGGraph(context, new PDGDataNode(
////                null, PyObject.SELF_EXPRESSION, "self",
////                "self", "self"));
////        pdg.mergeSequentialData(new PDGDataNode(astNode, PyObject.FIELD_ACCESS,
////                "self." + name, "UNKNOWN", name, true,
////                false), QUALIFIER);
////        return pdg;
//
//
//        return new PDGGraph(context);
//
//    }
//
//    private PDGGraph buildPDG(PDGNode control, String branch,
//                              LazyHole astNode) {
//        String name = ":[l" + (astNode).getInternalN() + "]";
//        String[] info = context.getLocalVariableInfo(name);
//        if (info != null) {
//            return new PDGGraph(context, new PDGLazyHole(
//                    astNode, astNode.getNodeType(), context.getTypeWrapper().getGuards().getValueOfTemplateVariable(name), info[0], info[1],
//                    name, true, false, false));
//        }
//        String type = context.getTypeWrapper().getGuards().getTypeOfTemplateVariable(name);
//        String value = context.getTypeWrapper().getGuards().getValueOfTemplateVariable(name);
//        if (type != null) {
//            context.addLocalVariable(name, "", type);
//            return new PDGGraph(context, new PDGLazyHole(
//                    astNode, astNode.getNodeType(), value, "" + astNode.getCharStartIndex(), type, name, true, false, false));
//        }
//        return new PDGGraph(context);
//    }

//    private String getFullNameOfAttribute(Attribute atr) {
//        if (atr.getInternalValue() instanceof Name)
//            return ((Name) atr.getInternalValue()).getInternalId() + atr.getInternalAttr();
//        else if (atr.getInternalValue() instanceof AlphanumericHole)
//            return (atr.getInternalValue()).toString() + atr.getInternalAttr();
//        else if (atr.getInternalValue() instanceof LazyHole)
//            return (atr.getInternalValue()).toString() + atr.getInternalAttr();
//        else if (atr.getInternalValue() instanceof Subscript) {
//            return getFullNameOfAttribute((Subscript) atr.getInternalValue()) + "." + atr.getInternalAttr();
//        } else if (atr.getInternalValue() instanceof Attribute) {
//            return getFullNameOfAttribute((Attribute) atr.getInternalValue()) + atr.getInternalAttr();
//        } else if (atr.getInternalValue() instanceof Call) {
//            return getFullNameOfAttribute((Call) atr.getInternalValue()) + atr.getInternalAttr();
//        } else if (atr.getInternalValue() instanceof Str) {
//            return ((Str) atr.getInternalValue()).getInternalS() + atr.getInternalAttr();
//        } else {
//            return "";
//        }
//    }
//
//    private String getFullNameOfAttribute(Call atr) {
//        if (atr.getInternalFunc() instanceof Name) {
//            return ((Name) atr.getInternalFunc()).getInternalId() + "()";
//        } else if (atr.getInternalFunc() instanceof Attribute) {
//            return getFullNameOfAttribute(((Attribute) atr.getInternalFunc())) + "()";
//        } else {
//            return "";
//        }
//    }
//
//    private String getFullNameOfAttribute(Subscript atr) {
//        if (atr.getInternalValue() instanceof Name && atr.getInternalSlice() instanceof Index) {
//            if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Num)
//                return ((Name) atr.getInternalValue()).getInternalId() + "[" + ((Num) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalN() + "]";
//            if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Name)
//                return ((Name) atr.getInternalValue()).getInternalId() + "[" + ((Name) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalId() + "]";
//            else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Str)
//                return ((Name) atr.getInternalValue()).getInternalId() + "[" + ((Str) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalS() + "]";
//            else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof BinOp)
//                return ((Name) atr.getInternalValue()).getInternalId() + "[" + ((BinOp) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalRight()
//                        + ((BinOp) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalOp().name() +
//                        ((BinOp) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalLeft() + "]";
//            else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Subscript)
//                return ((Name) atr.getInternalValue()).getInternalId() + "[" + getFullNameOfAttribute((Subscript) ((Index) atr.getInternalSlice()).getInternalValue()) + "]";
//            else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof List) {
//                return ((Name) atr.getInternalValue()).getInternalId() + "[]";
//            } else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Attribute) {
//                return ((Name) atr.getInternalValue()).getInternalId() + "[" + getFullNameOfAttribute((Attribute) ((Index) atr.getInternalSlice()).getInternalValue()) + "]";
//            } else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Call) {
//                return ((Name) atr.getInternalValue()).getInternalId() + "[" + getFullNameOfAttribute((Call) ((Index) atr.getInternalSlice()).getInternalValue()) + "]";
//            } else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Str)
//                return ((Name) atr.getInternalValue()).getInternalId() + "[" + ((Str) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalS() + "]";
//            else
//                return ((Name) atr.getInternalValue()).getInternalId() + "[" + "]";
//        } else if (atr.getInternalValue() instanceof AlphanumericHole)
//            return (atr.getInternalValue()).toString() + "[" + ((Num) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalN() + "]";
//        else if (atr.getInternalValue() instanceof LazyHole)
//            return (atr.getInternalValue()).toString() + "[" + ((Num) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalN() + "]";
//        else if (atr.getInternalValue() instanceof Subscript && atr.getInternalSlice() instanceof Index) {
//            if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Num)
//                return getFullNameOfAttribute((Subscript) atr.getInternalValue()) + "[" + ((Num) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalN() + "]";
//            else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Name)
//                return getFullNameOfAttribute((Subscript) atr.getInternalValue()) + "[" + ((Name) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalId() + "]";
//            else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Attribute) {
//                return getFullNameOfAttribute((Subscript) atr.getInternalValue()) + "[" + getFullNameOfAttribute((Attribute) ((Index) atr.getInternalSlice()).getInternalValue()) + "]";
//            } else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Str) {
//                return getFullNameOfAttribute((Subscript) atr.getInternalValue()) + "[" + ((Str) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalS() + "]";
//            } else
//                return getFullNameOfAttribute((Subscript) atr.getInternalValue()) + "[" + "]";
//        } else if (atr.getInternalSlice() instanceof ExtSlice) {
//            return "::";
//        } else if (atr.getInternalSlice() instanceof Slice) {
//            return ":";
//        } else if (atr.getInternalValue() instanceof Attribute) {
//            if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Num)
//                return getFullNameOfAttribute((Attribute) atr.getInternalValue()) + "[" + ((Num) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalN() + "]";
//            else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Name)
//                return getFullNameOfAttribute((Attribute) atr.getInternalValue()) + "[" + ((Name) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalId() + "]";
//            else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Subscript)
//                return getFullNameOfAttribute((Attribute) atr.getInternalValue()) + "[ ]";
//            else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Call) {
//                return getFullNameOfAttribute((Attribute) atr.getInternalValue()) + "[" + getFullNameOfAttribute((Call) ((Index) atr.getInternalSlice()).getInternalValue()) + "]";
//            } else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Attribute) {
//                return getFullNameOfAttribute((Attribute) atr.getInternalValue()) + "[" + getFullNameOfAttribute((Attribute) ((Index) atr.getInternalSlice()).getInternalValue()) + "]";
//            } else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof UnaryOp) {
//                return getFullNameOfAttribute((Attribute) atr.getInternalValue()) + "[" + ((UnaryOp) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalOp().toString() + "]";
//            } else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Str) {
//                return getFullNameOfAttribute((Attribute) atr.getInternalValue()) + "[" + ((Str) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalS() + "]";
//            } else
//                return getFullNameOfAttribute((Attribute) atr.getInternalValue()) + "[ ]";
//        } else if (atr.getInternalValue() instanceof Subscript) {
//            if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Num)
//                return getFullNameOfAttribute((Subscript) atr.getInternalValue()) + "[" + ((Num) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalN() + "]";
//            else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Name)
//                return getFullNameOfAttribute((Subscript) atr.getInternalValue()) + "[" + ((Name) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalId() + "]";
//            else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Subscript)
//                return getFullNameOfAttribute((Subscript) atr.getInternalValue()) + "[ ]";
//            else
//                return getFullNameOfAttribute((Subscript) atr.getInternalValue()) + "[" + ((Str) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalS() + "]";
//        } else if (atr.getInternalValue() instanceof Call) {
//            if (((Call) atr.getInternalValue()).getInternalFunc() instanceof Name) {
//                String fuName = ((Name) ((Call) atr.getInternalValue()).getInternalFunc()).getInternalId() + "()";
//                if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Num)
//                    return fuName + "[" + ((Num) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalN() + "]";
//                else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Name)
//                    return fuName + "[" + ((Name) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalId() + "]";
//                else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Subscript)
//                    return fuName + "[ ]";
//                else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Str)
//                    return fuName + "[" + ((Str) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalS() + "]";
//                else
//                    return fuName + "[" + "]";
//            } else {
//                if (atr.getInternalSlice() instanceof Index && ((Call) atr.getInternalValue()).getInternalFunc() instanceof Attribute) {
//                    if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Num)
//                        return getFullNameOfAttribute((Attribute) ((Call) atr.getInternalValue()).getInternalFunc())
//                                + "[" + ((Num) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalN() + "]";
//                    else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Name)
//                        return getFullNameOfAttribute((Attribute) ((Call) atr.getInternalValue()).getInternalFunc())
//                                + "[" + ((Name) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalId() + "]";
//                    else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Subscript)
//                        return getFullNameOfAttribute((Attribute) ((Call) atr.getInternalValue()).getInternalFunc())
//                                + "[ ]";
//
//                    else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Str)
//                        return getFullNameOfAttribute((Attribute) ((Call) atr.getInternalValue()).getInternalFunc())
//                                + "[" + ((Str) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalS() + "]";
//                    else
//                        return getFullNameOfAttribute((Attribute) ((Call) atr.getInternalValue()).getInternalFunc())
//                                + "[" + "]";
//                } else if (((Call) atr.getInternalValue()).getInternalFunc() instanceof Subscript)
//                    return getFullNameOfAttribute((Subscript) ((Call) atr.getInternalValue()).getInternalFunc());
//                else if (((Call) atr.getInternalValue()).getInternalFunc() instanceof Attribute) {
//                    return getFullNameOfAttribute((Attribute) ((Call) atr.getInternalValue()).getInternalFunc());
//                } else {
//                    return "";
//                }
//
//            }
//        } else {
//
//            return "[]";
//        }
//    }
}


