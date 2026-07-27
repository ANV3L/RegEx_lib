// package com.kirusha.regex;

// import java.awt.BasicStroke;
// import java.awt.Color;
// import java.awt.Font;
// import java.awt.FontMetrics;
// import java.awt.GradientPaint;
// import java.awt.Graphics2D;
// import java.awt.RenderingHints;
// import java.awt.geom.Path2D;
// import java.awt.image.BufferedImage;
// import java.io.File;
// import java.io.FileWriter;
// import java.io.IOException;
// import java.io.PrintWriter;
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.HashMap;
// import java.util.HashSet;
// import java.util.LinkedList;
// import java.util.List;
// import java.util.Map;
// import java.util.Objects;
// import java.util.Queue;
// import java.util.Set;

// import javax.imageio.ImageIO;

// import org.aspectj.lang.annotation.After;
// import org.aspectj.lang.annotation.AfterReturning;
// import org.aspectj.lang.annotation.Aspect;
// import org.aspectj.lang.annotation.Before;
// import org.aspectj.lang.annotation.Pointcut;

// import com.kirusha.regex.lexer.Token;
// import com.kirusha.regex.nfa.NFA;
// import com.kirusha.regex.nfa.NFAFragment;
// import com.kirusha.regex.nfa.NFAState;
// import com.kirusha.regex.parser.ParserResult;
// import com.kirusha.regex.parser.ast.ASTNode;
// import com.kirusha.regex.parser.ast.AlternationNode;
// import com.kirusha.regex.parser.ast.BackReferenceNode;
// import com.kirusha.regex.parser.ast.BinaryNode;
// import com.kirusha.regex.parser.ast.CharClassNode;
// import com.kirusha.regex.parser.ast.ConcatenationNode;
// import com.kirusha.regex.parser.ast.EpsilonNode;
// import com.kirusha.regex.parser.ast.GroupNode;
// import com.kirusha.regex.parser.ast.KleeneStarNode;
// import com.kirusha.regex.parser.ast.LiteralNode;
// import com.kirusha.regex.parser.ast.PalindromizationNode;
// import com.kirusha.regex.parser.ast.RepeatNode;
// import com.kirusha.regex.parser.ast.UnaryNode;

// @Aspect
// public class LogAspects {

//     private final List<Token> collectedTokens = new ArrayList<>();

//     @Before("execution(public static void Main.main(String[]))")
//     public void initialize() {
//         System.out.println("Check started");

//         // Создаём директории
//         new File("data").mkdirs();
//         new File("results").mkdirs();

//         // Инициализируем tokens.csv
//         try (PrintWriter writer = new PrintWriter(new FileWriter("data/tokens.csv", false))) {
//             writer.println("token,string");
//         } catch (IOException e) {
//             System.err.println("Failed to initialize tokens.csv: " + e.getMessage());
//         }

//         // Генерируем примеры NFAFragment для каждого оператора
//         generateNFAFragmentExamples();
//     }

//     @After("execution(public static void Main.main(String[]))")
//     public void terminate() {
//         System.out.println("prog ended");
//         generateTokensImage();
//     }

//     // ═══════════════════════════════════════════════════════════
//     // ГЕНЕРАЦИЯ ПРИМЕРОВ NFA ФРАГМЕНТОВ
//     // ═══════════════════════════════════════════════════════════

//     private void generateNFAFragmentExamples() {
//         System.out.println("Generating NFA Fragment examples...");

//         // Создаём простые литералы для примеров
//         NFAFragment a = createLiteral("a", 0);
//         NFAFragment b = createLiteral("b", 10);
//         NFAFragment c = createLiteral("c", 20);

//         int exampleNum = 1;

//         // 1. Literal
//         visualizeFragment(a, exampleNum++, "Literal", "a");

//         // 2. Epsilon
//         NFAFragment epsilon = createEpsilon(30);
//         visualizeFragment(epsilon, exampleNum++, "Epsilon", "ε");

//         // 3. Concatenation: ab
//         NFAFragment concat = createConcatenation(
//                 createLiteral("a", 40),
//                 createLiteral("b", 50));
//         visualizeFragment(concat, exampleNum++, "Concatenation", "a·b");

//         // 4. Alternation: a|b
//         NFAFragment alt = createAlternation(
//                 createLiteral("a", 60),
//                 createLiteral("b", 70),
//                 80);
//         visualizeFragment(alt, exampleNum++, "Alternation", "a|b");

//         // 5. Kleene Star: a*
//         NFAFragment star = createKleeneStar(
//                 createLiteral("a", 90),
//                 100);
//         visualizeFragment(star, exampleNum++, "KleeneStar", "a*");

//         // 6. Repeat: a{3}
//         NFAFragment repeat = createRepeat(
//                 createLiteral("a", 110),
//                 3,
//                 120);
//         visualizeFragment(repeat, exampleNum++, "Repeat", "a{3}");

//         // 7. CharClass: [abc]
//         NFAFragment charClass = createCharClass(Arrays.asList("a", "b", "c"), 150);
//         visualizeFragment(charClass, exampleNum++, "CharClass", "[abc]");

//         // 8. Group: (a)
//         NFAFragment group = createGroup(
//                 createLiteral("a", 160),
//                 1,
//                 170);
//         visualizeFragment(group, exampleNum++, "Group", "(1:a)");

//         // 9. BackReference: \1
//         NFAFragment backref = createBackReference(1, 180);
//         visualizeFragment(backref, exampleNum++, "BackReference", "\\1");

//         System.out.println("NFA Fragment examples generated!");
//     }

//     // ═══════════════════════════════════════════════════════════
//     // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ДЛЯ СОЗДАНИЯ ФРАГМЕНТОВ
//     // ═══════════════════════════════════════════════════════════

//     private NFAFragment createLiteral(String symbol, int stateIdStart) {
//         NFAState start = new NFAState(stateIdStart);
//         NFAState accept = new NFAState(stateIdStart + 1);
//         start.addTransition(symbol, accept);
//         return new NFAFragment(start, accept);
//     }

//     private NFAFragment createEpsilon(int stateIdStart) {
//         NFAState start = new NFAState(stateIdStart);
//         NFAState accept = new NFAState(stateIdStart + 1);
//         start.addEpsilonTransition(accept);
//         return new NFAFragment(start, accept);
//     }

//     private NFAFragment createConcatenation(NFAFragment left, NFAFragment right) {
//         left.getAccept().addEpsilonTransition(right.getStart());
//         return new NFAFragment(left.getStart(), right.getAccept());
//     }

//     private NFAFragment createAlternation(NFAFragment left, NFAFragment right, int stateIdStart) {
//         NFAState newStart = new NFAState(stateIdStart);
//         NFAState newAccept = new NFAState(stateIdStart + 1);

//         newStart.addEpsilonTransition(left.getStart());
//         newStart.addEpsilonTransition(right.getStart());

//         left.getAccept().addEpsilonTransition(newAccept);
//         right.getAccept().addEpsilonTransition(newAccept);

//         return new NFAFragment(newStart, newAccept);
//     }

//     private NFAFragment createKleeneStar(NFAFragment child, int stateIdStart) {
//         NFAState newStart = new NFAState(stateIdStart);
//         NFAState newAccept = new NFAState(stateIdStart + 1);

//         newStart.addEpsilonTransition(child.getStart());
//         newStart.addEpsilonTransition(newAccept);

//         child.getAccept().addEpsilonTransition(child.getStart());
//         child.getAccept().addEpsilonTransition(newAccept);

//         return new NFAFragment(newStart, newAccept);
//     }

//     private NFAFragment createRepeat(NFAFragment template, int count, int stateIdStart) {
//         int currentId = stateIdStart;
//         NFAFragment result = null;

//         for (int i = 0; i < count; i++) {
//             NFAFragment copy = createLiteral(template.getStart().getTransitions().keySet().iterator().next(),
//                     currentId);
//             currentId += 2;

//             if (result == null) {
//                 result = copy;
//             } else {
//                 result = createConcatenation(result, copy);
//             }
//         }

//         return result;
//     }

//     private NFAFragment createCharClass(List<String> symbols, int stateIdStart) {
//         NFAState start = new NFAState(stateIdStart);
//         NFAState accept = new NFAState(stateIdStart + 1);

//         for (String symbol : symbols) {
//             start.addTransition(symbol, accept);
//         }

//         return new NFAFragment(start, accept);
//     }

//     private NFAFragment createGroup(NFAFragment child, int groupNum, int stateIdStart) {
//         NFAState start = new NFAState(stateIdStart);
//         start.setGroupOpen(groupNum);
//         start.addEpsilonTransition(child.getStart());

//         NFAState accept = new NFAState(stateIdStart + 1);
//         accept.setGroupClose(groupNum);
//         child.getAccept().addEpsilonTransition(accept);

//         return new NFAFragment(start, accept);
//     }

//     private NFAFragment createBackReference(int groupNum, int stateIdStart) {
//         NFAState start = new NFAState(stateIdStart);
//         NFAState accept = new NFAState(stateIdStart + 1);
//         start.addBackrefTransition(groupNum, accept);
//         return new NFAFragment(start, accept);
//     }

//     // ═══════════════════════════════════════════════════════════
//     // ВИЗУАЛИЗАЦИЯ ФРАГМЕНТА
//     // ═══════════════════════════════════════════════════════════

//     private void visualizeFragment(NFAFragment fragment, int number, String operatorName, String example) {
//         // Текстовое представление
//         writeFragmentText(fragment, number, operatorName, example);

//         // Графическое представление
//         generateFragmentImage(fragment, number, operatorName, example);
//     }

//     private void writeFragmentText(NFAFragment fragment, int number, String operatorName, String example) {
//         StringBuilder sb = new StringBuilder();
//         sb.append("NFA Fragment #").append(number).append(" - ").append(operatorName).append("\n");
//         sb.append("Example: ").append(example).append("\n");
//         sb.append("Start: ").append(fragment.getStart()).append("\n");
//         sb.append("Accept: ").append(fragment.getAccept()).append("\n\n");

//         // Собираем все состояния
//         Set<NFAState> states = collectStates(fragment.getStart());

//         sb.append("States:\n");
//         for (NFAState state : states) {
//             sb.append("  ").append(state);

//             if (state.equals(fragment.getStart()))
//                 sb.append(" [START]");
//             if (state.equals(fragment.getAccept()))
//                 sb.append(" [ACCEPT]");
//             if (state.getGroupOpen() != -1)
//                 sb.append(" [GROUP " + state.getGroupOpen() + " OPEN]");
//             if (state.getGroupClose() != -1)
//                 sb.append(" [GROUP " + state.getGroupClose() + " CLOSE]");

//             sb.append(":\n");

//             // Symbol transitions
//             for (Map.Entry<String, Set<NFAState>> entry : state.getTransitions().entrySet()) {
//                 for (NFAState target : entry.getValue()) {
//                     sb.append("    --'").append(entry.getKey()).append("'--> ").append(target).append("\n");
//                 }
//             }

//             // Epsilon transitions
//             for (NFAState target : state.getEpsilonTransitions()) {
//                 sb.append("    --ε--> ").append(target).append("\n");
//             }

//             // Backref transitions
//             for (Map.Entry<Integer, NFAState> entry : state.getBackrefTransitions().entrySet()) {
//                 sb.append("    --\\").append(entry.getKey()).append("--> ").append(entry.getValue()).append("\n");
//             }
//         }

//         try {
//             try (FileWriter writer = new FileWriter(new File("data", number + "_" + operatorName + ".txt"))) {
//                 writer.write(sb.toString());
//             }
//         } catch (IOException e) {
//             System.err.println("Failed to save fragment text: " + e.getMessage());
//         }
//     }

//     private void generateFragmentImage(NFAFragment fragment, int number, String operatorName, String example) {
//         int logicalWidth = 900;
//         int logicalHeight = 600;
//         int scale = 2;

//         BufferedImage image = new BufferedImage(logicalWidth * scale, logicalHeight * scale,
//                 BufferedImage.TYPE_INT_ARGB);
//         Graphics2D g2d = image.createGraphics();

//         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//         g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//         g2d.scale(scale, scale);

//         // Background
//         GradientPaint bgGrad = new GradientPaint(0, 0, new Color(24, 24, 30), 0, logicalHeight, new Color(14, 14, 18));
//         g2d.setPaint(bgGrad);
//         g2d.fillRect(0, 0, logicalWidth, logicalHeight);

//         // Header
//         g2d.setColor(new Color(36, 36, 45, 220));
//         g2d.fillRoundRect(20, 20, logicalWidth - 40, 80, 12, 12);

//         g2d.setColor(Color.WHITE);
//         g2d.setFont(new Font("Segoe UI", Font.BOLD, 20));
//         g2d.drawString("Fragment #" + number + " - " + operatorName, 40, 50);

//         g2d.setFont(new Font("Consolas", Font.PLAIN, 14));
//         g2d.setColor(new Color(170, 175, 190));
//         g2d.drawString("Example: " + example, 40, 80);

//         // Draw NFA graph
//         drawNFAGraph(g2d, fragment, 50, 130, logicalWidth - 100, logicalHeight - 160);

//         g2d.dispose();

//         try {
//             ImageIO.write(image, "png", new File("results", number + "_" + operatorName + ".png"));
//         } catch (IOException e) {
//             System.err.println("Failed to save fragment image: " + e.getMessage());
//         }
//     }

//     // ═══════════════════════════════════════════════════════════
//     // РИСОВАНИЕ ГРАФА NFA
//     // ═══════════════════════════════════════════════════════════

//     // ═══════════════════════════════════════════════════════════
//     // РИСОВАНИЕ ГРАФА NFA (С ПОДДЕРЖКОЙ ДВУСТОРОННИХ ПЕРЕХОДОВ)
//     // ═══════════════════════════════════════════════════════════

//     private static class StatePair {
//         NFAState from, to;

//         StatePair(NFAState from, NFAState to) {
//             this.from = from;
//             this.to = to;
//         }

//         @Override
//         public boolean equals(Object o) {
//             if (!(o instanceof StatePair))
//                 return false;
//             StatePair p = (StatePair) o;
//             return from.equals(p.from) && to.equals(p.to);
//         }

//         @Override
//         public int hashCode() {
//             return Objects.hash(from, to);
//         }
//     }

//     private static class EdgeInfo {
//         String label;
//         EdgeType type;

//         EdgeInfo(String label, EdgeType type) {
//             this.label = label;
//             this.type = type;
//         }
//     }

//     private enum EdgeType {
//         SYMBOL, EPSILON, BACKREF
//     }

//     private void drawNFAGraph(Graphics2D g2d, NFAFragment fragment, int x, int y, int width, int height) {
//         // Собираем все состояния
//         Set<NFAState> allStates = collectStates(fragment.getStart());

//         // Разбиваем на слои (BFS)
//         Map<NFAState, Integer> layers = new HashMap<>();
//         Map<Integer, List<NFAState>> layerStates = new HashMap<>();

//         Queue<NFAState> queue = new LinkedList<>();
//         queue.add(fragment.getStart());
//         layers.put(fragment.getStart(), 0);

//         while (!queue.isEmpty()) {
//             NFAState current = queue.poll();
//             int layer = layers.get(current);

//             layerStates.computeIfAbsent(layer, k -> new ArrayList<>()).add(current);

//             Set<NFAState> neighbors = new HashSet<>();
//             neighbors.addAll(current.getEpsilonTransitions());
//             for (Set<NFAState> targets : current.getTransitions().values()) {
//                 neighbors.addAll(targets);
//             }
//             neighbors.addAll(current.getBackrefTransitions().values());

//             for (NFAState next : neighbors) {
//                 if (!layers.containsKey(next)) {
//                     layers.put(next, layer + 1);
//                     queue.add(next);
//                 }
//             }
//         }

//         int maxLayer = layerStates.keySet().stream().max(Integer::compare).orElse(0);

//         // Вычисляем позиции
//         Map<NFAState, Integer> stateX = new HashMap<>();
//         Map<NFAState, Integer> stateY = new HashMap<>();

//         int layerGap = width / (maxLayer + 1);
//         int stateRadius = 25;

//         for (int layer = 0; layer <= maxLayer; layer++) {
//             List<NFAState> statesInLayer = layerStates.getOrDefault(layer, new ArrayList<>());
//             int numStates = statesInLayer.size();

//             int layerX = x + layerGap * (layer + 1);
//             int startY = y + height / 2 - (numStates - 1) * 60 / 2;

//             for (int i = 0; i < numStates; i++) {
//                 NFAState state = statesInLayer.get(i);
//                 stateX.put(state, layerX);
//                 stateY.put(state, startY + i * 60);
//             }
//         }

//         // Собираем все рёбра в структуру
//         Map<StatePair, List<EdgeInfo>> edges = new HashMap<>();

//         for (NFAState state : allStates) {
//             // Symbol transitions
//             for (Map.Entry<String, Set<NFAState>> entry : state.getTransitions().entrySet()) {
//                 for (NFAState target : entry.getValue()) {
//                     if (stateX.containsKey(target)) {
//                         StatePair pair = new StatePair(state, target);
//                         edges.computeIfAbsent(pair, k -> new ArrayList<>())
//                                 .add(new EdgeInfo(entry.getKey(), EdgeType.SYMBOL));
//                     }
//                 }
//             }

//             // Epsilon transitions
//             for (NFAState target : state.getEpsilonTransitions()) {
//                 if (stateX.containsKey(target)) {
//                     StatePair pair = new StatePair(state, target);
//                     edges.computeIfAbsent(pair, k -> new ArrayList<>())
//                             .add(new EdgeInfo("ε", EdgeType.EPSILON));
//                 }
//             }

//             // Backref transitions
//             for (Map.Entry<Integer, NFAState> entry : state.getBackrefTransitions().entrySet()) {
//                 NFAState target = entry.getValue();
//                 if (stateX.containsKey(target)) {
//                     StatePair pair = new StatePair(state, target);
//                     edges.computeIfAbsent(pair, k -> new ArrayList<>())
//                             .add(new EdgeInfo("\\" + entry.getKey(), EdgeType.BACKREF));
//                 }
//             }
//         }

//         // Рисуем рёбра (с учётом двусторонних переходов)
//         Set<StatePair> drawnPairs = new HashSet<>();
//         for (Map.Entry<StatePair, List<EdgeInfo>> entry : edges.entrySet()) {
//             StatePair pair = entry.getKey();
//             StatePair reverse = new StatePair(pair.to, pair.from);

//             if (edges.containsKey(reverse) && !drawnPairs.contains(pair) && !drawnPairs.contains(reverse)) {
//                 // Двусторонний переход - рисуем две изогнутые стрелки
//                 List<EdgeInfo> forwardEdges = entry.getValue();
//                 List<EdgeInfo> reverseEdges = edges.get(reverse);

//                 drawBidirectionalEdges(g2d, pair.from, pair.to, forwardEdges, reverseEdges,
//                         stateX, stateY, stateRadius);

//                 drawnPairs.add(pair);
//                 drawnPairs.add(reverse);
//             } else if (!drawnPairs.contains(pair)) {
//                 // Односторонний переход - обычная стрелка
//                 drawUnidirectionalEdges(g2d, pair.from, pair.to, entry.getValue(),
//                         stateX, stateY, stateRadius);
//                 drawnPairs.add(pair);
//             }
//         }

//         // Рисуем состояния
//         for (NFAState state : allStates) {
//             int sx = stateX.get(state);
//             int sy = stateY.get(state);

//             Color stateColor;
//             if (state.equals(fragment.getStart())) {
//                 stateColor = new Color(46, 125, 50);
//             } else if (state.equals(fragment.getAccept())) {
//                 stateColor = new Color(178, 34, 34);
//             } else {
//                 stateColor = new Color(63, 81, 181);
//             }

//             g2d.setColor(stateColor);
//             g2d.fillOval(sx - stateRadius, sy - stateRadius, stateRadius * 2, stateRadius * 2);

//             g2d.setColor(Color.WHITE);
//             g2d.setStroke(new BasicStroke(2.0f));
//             g2d.drawOval(sx - stateRadius, sy - stateRadius, stateRadius * 2, stateRadius * 2);

//             if (state.equals(fragment.getAccept())) {
//                 g2d.drawOval(sx - stateRadius + 3, sy - stateRadius + 3, stateRadius * 2 - 6, stateRadius * 2 - 6);
//             }

//             g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
//             String label = String.valueOf(state.getId());
//             FontMetrics fm = g2d.getFontMetrics();
//             g2d.drawString(label, sx - fm.stringWidth(label) / 2, sy + 4);
//         }
//     }

//     private void drawUnidirectionalEdges(Graphics2D g2d, NFAState from, NFAState to,
//             List<EdgeInfo> edges, Map<NFAState, Integer> stateX,
//             Map<NFAState, Integer> stateY, int stateRadius) {
//         int sx = stateX.get(from);
//         int sy = stateY.get(from);
//         int tx = stateX.get(to);
//         int ty = stateY.get(to);

//         // Объединяем метки
//         StringBuilder label = new StringBuilder();
//         Color color = new Color(110, 180, 255);

//         for (int i = 0; i < edges.size(); i++) {
//             EdgeInfo edge = edges.get(i);
//             if (i > 0)
//                 label.append(", ");
//             label.append(edge.label);

//             if (edge.type == EdgeType.EPSILON) {
//                 color = new Color(150, 150, 160);
//             } else if (edge.type == EdgeType.BACKREF) {
//                 color = new Color(255, 183, 77);
//             }
//         }

//         g2d.setColor(color);
//         g2d.setStroke(new BasicStroke(2.0f));
//         drawArrow(g2d, sx, sy, tx, ty, stateRadius);

//         g2d.setColor(Color.WHITE);
//         g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
//         g2d.drawString(label.toString(), (sx + tx) / 2 - 5, (sy + ty) / 2 - 5);
//     }

//     private void drawBidirectionalEdges(Graphics2D g2d, NFAState from, NFAState to,
//             List<EdgeInfo> forwardEdges, List<EdgeInfo> reverseEdges,
//             Map<NFAState, Integer> stateX, Map<NFAState, Integer> stateY,
//             int stateRadius) {
//         int sx = stateX.get(from);
//         int sy = stateY.get(from);
//         int tx = stateX.get(to);
//         int ty = stateY.get(to);

//         int offset = 20; // Смещение для изгиба

//         // Forward edge (изгиб в одну сторону)
//         StringBuilder forwardLabel = new StringBuilder();
//         Color forwardColor = new Color(110, 180, 255);
//         for (int i = 0; i < forwardEdges.size(); i++) {
//             EdgeInfo edge = forwardEdges.get(i);
//             if (i > 0)
//                 forwardLabel.append(", ");
//             forwardLabel.append(edge.label);
//             if (edge.type == EdgeType.EPSILON)
//                 forwardColor = new Color(150, 150, 160);
//             else if (edge.type == EdgeType.BACKREF)
//                 forwardColor = new Color(255, 183, 77);
//         }

//         g2d.setColor(forwardColor);
//         g2d.setStroke(new BasicStroke(2.0f));
//         drawCurvedArrow(g2d, sx, sy, tx, ty, offset, stateRadius);

//         g2d.setColor(Color.WHITE);
//         g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
//         int[] labelPos = getCurvedEdgeLabelPosition(sx, sy, tx, ty, offset);
//         g2d.drawString(forwardLabel.toString(), labelPos[0], labelPos[1]);

//         // Reverse edge (изгиб в другую сторону)
//         StringBuilder reverseLabel = new StringBuilder();
//         Color reverseColor = new Color(110, 180, 255);
//         for (int i = 0; i < reverseEdges.size(); i++) {
//             EdgeInfo edge = reverseEdges.get(i);
//             if (i > 0)
//                 reverseLabel.append(", ");
//             reverseLabel.append(edge.label);
//             if (edge.type == EdgeType.EPSILON)
//                 reverseColor = new Color(150, 150, 160);
//             else if (edge.type == EdgeType.BACKREF)
//                 reverseColor = new Color(255, 183, 77);
//         }

//         g2d.setColor(reverseColor);
//         g2d.setStroke(new BasicStroke(2.0f));
//         drawCurvedArrow(g2d, tx, ty, sx, sy, offset, stateRadius);

//         g2d.setColor(Color.WHITE);
//         g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
//         labelPos = getCurvedEdgeLabelPosition(tx, ty, sx, sy, offset);
//         g2d.drawString(reverseLabel.toString(), labelPos[0], labelPos[1]);
//     }

//     private void drawCurvedArrow(Graphics2D g2d, int x1, int y1, int x2, int y2, int offset, int stateRadius) {
//         double angle = Math.atan2(y2 - y1, x2 - x1);
//         double perpAngle = angle + Math.PI / 2;

//         int startX = (int) (x1 + stateRadius * Math.cos(angle));
//         int startY = (int) (y1 + stateRadius * Math.sin(angle));
//         int endX = (int) (x2 - stateRadius * Math.cos(angle));
//         int endY = (int) (y2 - stateRadius * Math.sin(angle));

//         // Контрольная точка для кривой Безье
//         int midX = (startX + endX) / 2 + (int) (offset * Math.cos(perpAngle));
//         int midY = (startY + endY) / 2 + (int) (offset * Math.sin(perpAngle));

//         Path2D.Double path = new Path2D.Double();
//         path.moveTo(startX, startY);
//         path.quadTo(midX, midY, endX, endY);
//         g2d.draw(path);

//         // Стрелка на конце
//         double endAngle = Math.atan2(endY - midY, endX - midX);
//         int arrowSize = 10;
//         int[] xPoints = {
//                 endX,
//                 endX - (int) (arrowSize * Math.cos(endAngle - Math.PI / 6)),
//                 endX - (int) (arrowSize * Math.cos(endAngle + Math.PI / 6))
//         };
//         int[] yPoints = {
//                 endY,
//                 endY - (int) (arrowSize * Math.sin(endAngle - Math.PI / 6)),
//                 endY - (int) (arrowSize * Math.sin(endAngle + Math.PI / 6))
//         };

//         g2d.fillPolygon(xPoints, yPoints, 3);
//     }

//     private int[] getCurvedEdgeLabelPosition(int x1, int y1, int x2, int y2, int offset) {
//         double angle = Math.atan2(y2 - y1, x2 - x1);
//         double perpAngle = angle + Math.PI / 2;

//         int midX = (x1 + x2) / 2 + (int) ((offset * 0.7) * Math.cos(perpAngle));
//         int midY = (y1 + y2) / 2 + (int) ((offset * 0.7) * Math.sin(perpAngle));

//         return new int[] { midX - 10, midY + 3 };
//     }

//     private void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2, int stateRadius) {
//         // Вычисляем направление
//         double angle = Math.atan2(y2 - y1, x2 - x1);

//         // Сокращаем линию, чтобы не входила в круги
//         int startX = (int) (x1 + stateRadius * Math.cos(angle));
//         int startY = (int) (y1 + stateRadius * Math.sin(angle));
//         int endX = (int) (x2 - stateRadius * Math.cos(angle));
//         int endY = (int) (y2 - stateRadius * Math.sin(angle));

//         // Рисуем линию
//         g2d.drawLine(startX, startY, endX, endY);

//         // Рисуем стрелку
//         int arrowSize = 10;
//         int[] xPoints = {
//                 endX,
//                 endX - (int) (arrowSize * Math.cos(angle - Math.PI / 6)),
//                 endX - (int) (arrowSize * Math.cos(angle + Math.PI / 6))
//         };
//         int[] yPoints = {
//                 endY,
//                 endY - (int) (arrowSize * Math.sin(angle - Math.PI / 6)),
//                 endY - (int) (arrowSize * Math.sin(angle + Math.PI / 6))
//         };

//         g2d.fillPolygon(xPoints, yPoints, 3);
//     }

//     private Set<NFAState> collectStates(NFAState start) {
//         Set<NFAState> visited = new HashSet<>();
//         Queue<NFAState> queue = new LinkedList<>();
//         queue.add(start);

//         while (!queue.isEmpty()) {
//             NFAState current = queue.poll();
//             if (!visited.add(current))
//                 continue;

//             queue.addAll(current.getEpsilonTransitions());
//             for (Set<NFAState> targets : current.getTransitions().values()) {
//                 queue.addAll(targets);
//             }
//             queue.addAll(current.getBackrefTransitions().values());
//         }

//         return visited;
//     }

//     // ═══════════════════════════════════════════════════════════
//     // TOKENS VISUALIZATION
//     // ═══════════════════════════════════════════════════════════

//     @Pointcut("call(com.kirusha.regex.lexer.Token.new(..))")
//     public void tokenCreation() {
//     }

//     @AfterReturning(pointcut = "tokenCreation()", returning = "token")
//     public void logToken(Token token) {
//         synchronized (collectedTokens) {
//             collectedTokens.add(token);
//         }
//         try (PrintWriter writer = new PrintWriter(new FileWriter("data/tokens.csv", true))) {
//             writer.println(token.getType() + "," + token.getValue());
//         } catch (IOException e) {
//             System.err.println("Failed to write token: " + e.getMessage());
//         }
//     }

//     private synchronized void generateTokensImage() {
//         int logicalWidth = 900;
//         int logicalHeight = 600;
//         int scale = 2;
//         BufferedImage image = new BufferedImage(logicalWidth * scale, logicalHeight * scale,
//                 BufferedImage.TYPE_INT_ARGB);
//         Graphics2D g2d = image.createGraphics();

//         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//         g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//         g2d.scale(scale, scale);

//         g2d.setColor(new Color(24, 24, 28));
//         g2d.fillRect(0, 0, logicalWidth, logicalHeight);

//         g2d.setColor(new Color(36, 36, 42));
//         g2d.fillRect(0, 0, logicalWidth, 75);
//         g2d.setColor(Color.WHITE);
//         g2d.setFont(new Font("Segoe UI", Font.BOLD, 22));
//         g2d.drawString("Tokens", 30, 45);

//         g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
//         g2d.setColor(new Color(150, 150, 160));
//         g2d.drawString("Lexer Output", logicalWidth - 150, 45);

//         g2d.setColor(Color.WHITE);
//         g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
//         g2d.drawString("Token Stream", 30, 115);

//         int startX = 30, startY = 140, boxW = 80, boxH = 48, gap = 12, cols = 9;

//         List<Token> tokens;
//         synchronized (collectedTokens) {
//             tokens = new ArrayList<>(collectedTokens);
//         }

//         for (int i = 0; i < tokens.size(); i++) {
//             Token t = tokens.get(i);
//             int row = i / cols, col = i % cols;

//             if (row >= 7) {
//                 if (row == 7 && col == 0) {
//                     g2d.setColor(Color.LIGHT_GRAY);
//                     g2d.setFont(new Font("Segoe UI", Font.BOLD, 24));
//                     g2d.drawString("...", startX + col * (boxW + gap) + 30, startY + row * (boxH + gap) + 25);
//                 }
//                 continue;
//             }

//             int x = startX + col * (boxW + gap), y = startY + row * (boxH + gap);

//             Color boxBg;
//             String typeStr = t.getType().toString();

//             if (typeStr.contains("CHAR"))
//                 boxBg = new Color(34, 139, 87);
//             else if (typeStr.contains("NUMBER"))
//                 boxBg = new Color(204, 119, 34);
//             else if (typeStr.contains("STAR") || typeStr.contains("PLUS"))
//                 boxBg = new Color(178, 34, 34);
//             else if (typeStr.contains("BRACKET") || typeStr.contains("PAREN"))
//                 boxBg = new Color(30, 144, 255);
//             else
//                 boxBg = new Color(105, 105, 115);

//             g2d.setColor(boxBg);
//             g2d.fillRoundRect(x, y, boxW, boxH, 8, 8);

//             g2d.setColor(Color.WHITE);
//             g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
//             String val = t.getValue();
//             if (val == null || val.isEmpty())
//                 val = " ";
//             int valW = g2d.getFontMetrics().stringWidth(val);
//             g2d.drawString(val, x + (boxW - valW) / 2, y + 22);

//             g2d.setColor(new Color(230, 230, 240));
//             g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
//             String lbl = typeStr.length() > 12 ? typeStr.substring(0, 10) + ".." : typeStr;
//             int lblW = g2d.getFontMetrics().stringWidth(lbl);
//             g2d.drawString(lbl, x + (boxW - lblW) / 2, y + 38);
//         }

//         g2d.dispose();

//         try {
//             ImageIO.write(image, "png", new File("results", "tokens.png"));
//         } catch (IOException e) {
//             System.err.println("Failed to save tokens image: " + e.getMessage());
//         }
//     }

//     // ═══════════════════════════════════════════════════════════
//     // PARSER VISUALIZATION
//     // ═══════════════════════════════════════════════════════════

//     @Pointcut("execution(* com.kirusha.regex.parser.*.parse(..))")
//     public void parserParse() {
//     }

//     @AfterReturning(pointcut = "parserParse()", returning = "result")
//     public void logParserResult(ParserResult result) {
//         if (result != null && result.getRoot() != null) {
//             generateParserTreeImage(result);
//             writeTreeText(result.getRoot(), result.getOriginalInput());
//         }
//     }

//     private void writeTreeText(ASTNode root, String originalInput) {
//         StringBuilder sb = new StringBuilder();
//         sb.append("Regex: ").append(originalInput != null ? originalInput : "").append("\n.\n");
//         renderTreeTextHelper(root, "", true, sb);

//         try {
//             try (FileWriter writer = new FileWriter(new File("data", "parser.txt"))) {
//                 writer.write(sb.toString());
//             }
//             System.out.println("Parser tree saved to data/parser.txt");
//         } catch (IOException e) {
//             System.err.println("Failed to save parser tree: " + e.getMessage());
//         }
//     }

//     private void renderTreeTextHelper(ASTNode node, String prefix, boolean isLast, StringBuilder sb) {
//         if (node == null)
//             return;

//         sb.append(prefix).append(isLast ? "└── " : "├── ");
//         sb.append(getLabel(node)).append(" (").append(node.getClass().getSimpleName()).append(")\n");

//         List<ASTNode> children = getChildren(node);
//         for (int i = 0; i < children.size(); i++) {
//             renderTreeTextHelper(children.get(i), prefix + (isLast ? "    " : "│   "), i == children.size() - 1, sb);
//         }
//     }

//     private List<ASTNode> getChildren(ASTNode node) {
//         List<ASTNode> children = new ArrayList<>();
//         if (node instanceof BinaryNode) {
//             ASTNode left = ((BinaryNode) node).getLeft();
//             ASTNode right = ((BinaryNode) node).getRight();
//             if (left != null)
//                 children.add(left);
//             if (right != null)
//                 children.add(right);
//         } else if (node instanceof UnaryNode) {
//             ASTNode child = ((UnaryNode) node).getChild();
//             if (child != null)
//                 children.add(child);
//         }
//         return children;
//     }

//     private void generateParserTreeImage(ParserResult result) {
//         ASTNode rootNode = result.getRoot();
//         String originalInput = result.getOriginalInput();

//         BufferedImage tempImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
//         Graphics2D tempG = tempImg.createGraphics();
//         Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
//         tempG.setFont(labelFont);
//         FontMetrics fm = tempG.getFontMetrics();

//         ASTLayoutNode layoutRoot = new ASTLayoutNode(rootNode, fm);
//         tempG.dispose();

//         computeSubtreeWidth(layoutRoot);
//         int depth = computeDepth(layoutRoot);
//         int levelGap = 80, topPadding = 120, bottomPadding = 60, leftPadding = 50;

//         int logicalWidth = Math.max(600, layoutRoot.width + leftPadding * 2);
//         int logicalHeight = Math.max(400, topPadding + (depth - 1) * levelGap + bottomPadding);

//         int startX = (logicalWidth > layoutRoot.width + leftPadding * 2) ? (logicalWidth - layoutRoot.width) / 2
//                 : leftPadding;

//         assignCoordinates(layoutRoot, startX, topPadding, levelGap);

//         int scale = 2;
//         BufferedImage image = new BufferedImage(logicalWidth * scale, logicalHeight * scale,
//                 BufferedImage.TYPE_INT_ARGB);
//         Graphics2D g2d = image.createGraphics();

//         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//         g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//         g2d.scale(scale, scale);

//         GradientPaint bgGrad = new GradientPaint(0, 0, new Color(24, 24, 30), 0, logicalHeight, new Color(14, 14, 18));
//         g2d.setPaint(bgGrad);
//         g2d.fillRect(0, 0, logicalWidth, logicalHeight);

//         g2d.setColor(new Color(255, 255, 255, 8));
//         for (int x = 0; x < logicalWidth; x += 30)
//             g2d.drawLine(x, 0, x, logicalHeight);
//         for (int y = 0; y < logicalHeight; y += 30)
//             g2d.drawLine(0, y, logicalWidth, y);

//         drawEdges(g2d, layoutRoot);
//         drawNodes(g2d, layoutRoot, labelFont);

//         g2d.setColor(new Color(36, 36, 45, 220));
//         g2d.fillRoundRect(20, 20, logicalWidth - 40, 70, 12, 12);
//         g2d.setColor(new Color(255, 255, 255, 20));
//         g2d.setStroke(new BasicStroke(1.0f));
//         g2d.drawRoundRect(20, 20, logicalWidth - 40, 70, 12, 12);

//         g2d.setColor(Color.WHITE);
//         g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
//         g2d.drawString("AST Parser Tree", 40, 48);

//         g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
//         g2d.setColor(new Color(170, 175, 190));
//         g2d.drawString("Pattern:", 40, 72);

//         g2d.setFont(new Font("Consolas", Font.BOLD, 13));
//         g2d.setColor(new Color(110, 180, 255));
//         g2d.drawString(originalInput != null ? originalInput : "empty", 100, 72);

//         g2d.dispose();

//         try {
//             ImageIO.write(image, "png", new File("results", "parser.png"));
//             System.out.println("Parser tree image saved to results/parser.png");
//         } catch (IOException e) {
//             System.err.println("Failed to save parser image: " + e.getMessage());
//         }
//     }

//     // (Методы для AST остаются такими же, как в оригинале)
//     private int computeSubtreeWidth(ASTLayoutNode layoutNode) {
//         if (layoutNode.children.isEmpty()) {
//             layoutNode.width = layoutNode.nodeWidth;
//             return layoutNode.width;
//         }
//         int childrenWidth = 0;
//         for (int i = 0; i < layoutNode.children.size(); i++) {
//             childrenWidth += computeSubtreeWidth(layoutNode.children.get(i));
//             if (i < layoutNode.children.size() - 1)
//                 childrenWidth += 30;
//         }
//         layoutNode.width = Math.max(layoutNode.nodeWidth, childrenWidth);
//         return layoutNode.width;
//     }

//     private int computeDepth(ASTLayoutNode node) {
//         if (node == null)
//             return 0;
//         int maxChildDepth = 0;
//         for (ASTLayoutNode child : node.children) {
//             maxChildDepth = Math.max(maxChildDepth, computeDepth(child));
//         }
//         return 1 + maxChildDepth;
//     }

//     private void assignCoordinates(ASTLayoutNode layoutNode, int leftX, int y, int levelGap) {
//         layoutNode.y = y;
//         if (layoutNode.children.isEmpty()) {
//             layoutNode.x = leftX + layoutNode.width / 2;
//             return;
//         }
//         int childrenWidth = 0;
//         for (int i = 0; i < layoutNode.children.size(); i++) {
//             childrenWidth += layoutNode.children.get(i).width;
//             if (i < layoutNode.children.size() - 1)
//                 childrenWidth += 30;
//         }
//         int currentX = layoutNode.width > childrenWidth ? leftX + (layoutNode.width - childrenWidth) / 2 : leftX;
//         for (ASTLayoutNode child : layoutNode.children) {
//             assignCoordinates(child, currentX, y + levelGap, levelGap);
//             currentX += child.width + 30;
//         }
//         if (layoutNode.children.size() == 1) {
//             layoutNode.x = layoutNode.children.get(0).x;
//         } else {
//             layoutNode.x = (layoutNode.children.get(0).x + layoutNode.children.get(layoutNode.children.size() - 1).x)
//                     / 2;
//         }
//     }

//     private void drawEdges(Graphics2D g2d, ASTLayoutNode node) {
//         g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
//         for (ASTLayoutNode child : node.children) {
//             Path2D.Double path = new Path2D.Double();
//             path.moveTo(node.x, node.y + node.nodeHeight / 2);
//             int ctrlY = node.y + (child.y - node.nodeHeight / 2 - node.y) / 2;
//             path.curveTo(node.x, ctrlY, child.x, ctrlY, child.x, child.y - child.nodeHeight / 2);
//             g2d.setColor(new Color(120, 130, 160, 150));
//             g2d.draw(path);
//             drawEdges(g2d, child);
//         }
//     }

//     private void drawNodes(Graphics2D g2d, ASTLayoutNode layoutNode, Font font) {
//         int rx = layoutNode.x - layoutNode.nodeWidth / 2, ry = layoutNode.y - layoutNode.nodeHeight / 2;

//         Color baseColor, borderColor;
//         ASTNode node = layoutNode.node;

//         if (node instanceof AlternationNode) {
//             baseColor = new Color(103, 58, 183);
//             borderColor = new Color(149, 117, 205);
//         } else if (node instanceof ConcatenationNode) {
//             baseColor = new Color(63, 81, 181);
//             borderColor = new Color(121, 134, 203);
//         } else if (node instanceof GroupNode) {
//             baseColor = new Color(255, 112, 67);
//             borderColor = new Color(255, 160, 122);
//         } else if (node instanceof KleeneStarNode || node instanceof RepeatNode) {
//             baseColor = new Color(236, 64, 122);
//             borderColor = new Color(244, 143, 177);
//         } else if (node instanceof LiteralNode) {
//             baseColor = new Color(46, 125, 50);
//             borderColor = new Color(129, 199, 132);
//         } else if (node instanceof EpsilonNode) {
//             baseColor = new Color(117, 117, 117);
//             borderColor = new Color(189, 189, 189);
//         } else if (node instanceof CharClassNode) {
//             baseColor = new Color(0, 137, 123);
//             borderColor = new Color(77, 182, 172);
//         } else if (node instanceof BackReferenceNode) {
//             baseColor = new Color(230, 81, 0);
//             borderColor = new Color(255, 183, 77);
//         } else {
//             baseColor = new Color(84, 110, 122);
//             borderColor = new Color(144, 164, 174);
//         }

//         g2d.setColor(baseColor);
//         g2d.fillRoundRect(rx, ry, layoutNode.nodeWidth, layoutNode.nodeHeight, 8, 8);
//         g2d.setColor(new Color(255, 255, 255, 40));
//         g2d.setStroke(new BasicStroke(1.0f));
//         g2d.drawRoundRect(rx + 1, ry + 1, layoutNode.nodeWidth - 2, layoutNode.nodeHeight - 2, 6, 6);
//         g2d.setColor(borderColor);
//         g2d.setStroke(new BasicStroke(1.5f));
//         g2d.drawRoundRect(rx, ry, layoutNode.nodeWidth, layoutNode.nodeHeight, 8, 8);

//         g2d.setColor(Color.WHITE);
//         g2d.setFont(font);
//         FontMetrics fm = g2d.getFontMetrics();
//         g2d.drawString(layoutNode.label, layoutNode.x - fm.stringWidth(layoutNode.label) / 2,
//                 layoutNode.y + (fm.getAscent() - fm.getDescent()) / 2 + 1);

//         for (ASTLayoutNode child : layoutNode.children)
//             drawNodes(g2d, child, font);
//     }

//     private static String getLabel(ASTNode node) {
//         if (node == null)
//             return "null";
//         if (node instanceof AlternationNode)
//             return "|";
//         if (node instanceof ConcatenationNode)
//             return "•";
//         if (node instanceof GroupNode)
//             return "(" + ((GroupNode) node).getGroupNumber() + ")";
//         if (node instanceof KleeneStarNode)
//             return "*";
//         if (node instanceof LiteralNode)
//             return "'" + ((LiteralNode) node).getValue() + "'";
//         if (node instanceof RepeatNode)
//             return "{" + ((RepeatNode) node).getCount() + "}";
//         if (node instanceof PalindromizationNode)
//             return "^P";
//         if (node instanceof EpsilonNode)
//             return "ε";
//         if (node instanceof CharClassNode)
//             return ((CharClassNode) node).getSymbols().toString();
//         if (node instanceof BackReferenceNode)
//             return "\\" + ((BackReferenceNode) node).getGroupNumber();
//         return node.getClass().getSimpleName();
//     }

//     private static class ASTLayoutNode {
//         ASTNode node;
//         String label;
//         List<ASTLayoutNode> children = new ArrayList<>();
//         int x, y, width, nodeWidth, nodeHeight = 32;

//         ASTLayoutNode(ASTNode node, FontMetrics fm) {
//             this.node = node;
//             this.label = getLabel(node);
//             this.nodeWidth = Math.max(40, fm.stringWidth(this.label) + 24);

//             if (node instanceof BinaryNode) {
//                 ASTNode left = ((BinaryNode) node).getLeft();
//                 ASTNode right = ((BinaryNode) node).getRight();
//                 if (left != null)
//                     children.add(new ASTLayoutNode(left, fm));
//                 if (right != null)
//                     children.add(new ASTLayoutNode(right, fm));
//             } else if (node instanceof UnaryNode) {
//                 ASTNode child = ((UnaryNode) node).getChild();
//                 if (child != null)
//                     children.add(new ASTLayoutNode(child, fm));
//             }
//         }
//     }

//     // ═══════════════════════════════════════════════════════════
//     // FINAL NFA VISUALIZATION
//     // ═══════════════════════════════════════════════════════════

//     @Pointcut("execution(* com.kirusha.regex.nfa.ThompsonBuilder.build(..))")
//     public void nfaBuild() {
//     }

//     @AfterReturning(pointcut = "nfaBuild()", returning = "nfa")
//     public void logNFA(NFA nfa) {
//         if (nfa != null) {
//             writeNFAText(nfa);
//             generateNFAImage(nfa);
//         }
//     }

//     private void writeNFAText(NFA nfa) {
//         StringBuilder sb = new StringBuilder();
//         sb.append("Final NFA\n=========\n");
//         sb.append("Start: ").append(nfa.getStartState()).append("\n");
//         sb.append("Accept: ").append(nfa.getAcceptState()).append("\n");
//         sb.append("States: ").append(nfa.getStates().size()).append("\n");
//         sb.append("Alphabet: ").append(nfa.getAlphabet()).append("\n");
//         sb.append("Groups: ").append(nfa.getGroupCount()).append("\n\n");

//         sb.append("All States:\n");
//         for (NFAState state : nfa.getStates()) {
//             sb.append("  ").append(state);
//             if (state.equals(nfa.getStartState()))
//                 sb.append(" [START]");
//             if (state.equals(nfa.getAcceptState()))
//                 sb.append(" [ACCEPT]");
//             if (state.getGroupOpen() != -1)
//                 sb.append(" [GROUP " + state.getGroupOpen() + " OPEN]");
//             if (state.getGroupClose() != -1)
//                 sb.append(" [GROUP " + state.getGroupClose() + " CLOSE]");
//             sb.append(":\n");

//             for (Map.Entry<String, Set<NFAState>> entry : state.getTransitions().entrySet()) {
//                 for (NFAState target : entry.getValue()) {
//                     sb.append("    --'").append(entry.getKey()).append("'--> ").append(target).append("\n");
//                 }
//             }
//             for (NFAState target : state.getEpsilonTransitions()) {
//                 sb.append("    --ε--> ").append(target).append("\n");
//             }
//             for (Map.Entry<Integer, NFAState> entry : state.getBackrefTransitions().entrySet()) {
//                 sb.append("    --\\").append(entry.getKey()).append("--> ").append(entry.getValue()).append("\n");
//             }
//         }

//         try {
//             try (FileWriter writer = new FileWriter(new File("data", "nfa.txt"))) {
//                 writer.write(sb.toString());
//             }
//             System.out.println("Final NFA saved to data/nfa.txt");
//         } catch (IOException e) {
//             System.err.println("Failed to save NFA text: " + e.getMessage());
//         }
//     }

//     private void generateNFAImage(NFA nfa) {
//         int logicalWidth = 1200, logicalHeight = 800, scale = 2;
//         BufferedImage image = new BufferedImage(logicalWidth * scale, logicalHeight * scale,
//                 BufferedImage.TYPE_INT_ARGB);
//         Graphics2D g2d = image.createGraphics();

//         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//         g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//         g2d.scale(scale, scale);

//         GradientPaint bgGrad = new GradientPaint(0, 0, new Color(24, 24, 30), 0, logicalHeight, new Color(14, 14, 18));
//         g2d.setPaint(bgGrad);
//         g2d.fillRect(0, 0, logicalWidth, logicalHeight);

//         g2d.setColor(new Color(36, 36, 45, 220));
//         g2d.fillRoundRect(20, 20, logicalWidth - 40, 70, 12, 12);

//         g2d.setColor(Color.WHITE);
//         g2d.setFont(new Font("Segoe UI", Font.BOLD, 22));
//         g2d.drawString("Final NFA", 40, 50);

//         g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
//         g2d.setColor(new Color(170, 175, 190));
//         g2d.drawString("States: " + nfa.getStates().size() + " | Alphabet: " + nfa.getAlphabet(), 40, 75);

//         NFAFragment fragment = new NFAFragment(nfa.getStartState(), nfa.getAcceptState());
//         drawNFAGraph(g2d, fragment, 50, 110, logicalWidth - 100, logicalHeight - 130);

//         g2d.dispose();

//         try {
//             ImageIO.write(image, "png", new File("results", "nfa.png"));
//             System.out.println("Final NFA image saved to results/nfa.png");
//         } catch (IOException e) {
//             System.err.println("Failed to save NFA image: " + e.getMessage());
//         }
//     }
// }