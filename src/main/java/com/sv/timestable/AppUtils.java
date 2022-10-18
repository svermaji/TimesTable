package com.sv.timestable;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class AppUtils {

    private static final Map<Integer, List<Integer[]>> gameLevelSequences = new ConcurrentHashMap<>();
    public static GameButton lastButton;

    private AppUtils() {
    }

    private static void prepareGameSequences(int[][] gameSequences) {
        Arrays.stream(gameSequences).forEach(arr -> {
            int sum = Arrays.stream(arr).reduce(0, Integer::sum);
            if (gameLevelSequences.containsKey(sum)) {
                List<Integer[]> list = gameLevelSequences.get(sum);
                list.add(Arrays.stream(arr).boxed().toArray(Integer[]::new));
            } else {
                List<Integer[]> list = new ArrayList<>();
                list.add(Arrays.stream(arr).boxed().toArray(Integer[]::new));
                gameLevelSequences.put(sum, list);
            }
        });
    }

    public static Integer[] getRandomGameSeq(GameInfo gi, int[][] gameSequences) {
        if (gameLevelSequences.isEmpty()) {
            prepareGameSequences(gameSequences);
        }
        List<Integer[]> list = gameLevelSequences.get(gi.getRows() * gi.getCols());
        Random rand = new Random();
        return list.get(rand.nextInt(list.size()));
    }

    public static Color getColor(String s) {
        switch (s) {
            case "red":
                return Color.red;
            case "green":
                return Color.green;
            case "blue":
                return Color.blue;
            case "purple":
                return new Color(102, 0, 153);
            case "magenta":
                return Color.magenta;
            case "orange":
                return Color.orange;
        }
        return Color.black;
    }
}
