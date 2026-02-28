package cn.taotxi.Makemoney.util;

import java.util.HashSet;
import java.util.List;

public class CommonUtil {
    public static boolean hasIntersection(List<String> list1, List<String> list2) {
        if (list1.isEmpty() || list2.isEmpty()) return false;
        // 将小的表放进哈希表
        if (list1.size() > list2.size()) {
            List<String> temp = list1;
            list1 = list2;
            list2 = temp;
        }
        // Now list1.size() <= list2.size()
        if (list1.size() == 1) {
            return list2.contains(list1.get(0));
        }
        HashSet<String> hashSet = new HashSet<>(list1);
        for (String str : list2) {
            if (hashSet.contains(str)) {
                return true;
            }
        }
        return false;
    }
    
}
