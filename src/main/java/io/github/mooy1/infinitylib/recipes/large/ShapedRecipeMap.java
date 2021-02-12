package io.github.mooy1.infinitylib.recipes.large;

import io.github.mooy1.infinitylib.items.StackUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A recipe map with 3x3 shaped (or shapeless) recipes. It will detect the shape of recipes on its own.
 * 
 * @author Mooy1
 */
public final class ShapedRecipeMap {
    
    private final Map<ShapedRecipe, ItemStack> map = new HashMap<>();

    public void put(@Nonnull ItemStack[] recipe, @Nonnull ItemStack output) {
        Validate.notNull(recipe);
        Validate.isTrue(recipe.length == 9);
        Validate.notNull(output);
        
        this.map.put(new ShapedRecipe(recipe), output);
    }
    
    @Nullable
    public ShapedOutput get(@Nonnull ItemStack[] input) {
        Validate.isTrue(input.length == 9);
        
        int[] amounts = new int[9];
        for (int i = 0 ; i < 9 ; i++) {
            if (input[i] != null) {
                amounts[i] = input[i].getAmount();
            }
        }
        
        ItemStack output = this.map.get(new ShapedRecipe(input));
        return output == null ? null : new ShapedOutput(output, amounts);
    }
    
    @Nullable
    public ShapedOutput get(@Nonnull BlockMenu menu, @Nonnull int[] slots) {
        Validate.isTrue(slots.length == 9);
        
        ItemStack[] stacks = new ItemStack[9];
        int[] amounts = new int[9];
        for (int i = 0 ; i < 9 ; i++) {
            ItemStack item = menu.getItemInSlot(slots[i]);
            if (item != null) {
                amounts[i] = (stacks[i] = item).getAmount();
            }
        }
        
        ItemStack output = this.map.get(new ShapedRecipe(stacks));
        return output == null ? null : new ShapedOutput(output, amounts);
    }
    
    public int size() {
        return this.map.size();
    }

    /**
     * A shaped, 3x3 recipe which detects the shape on its own and compares ids and shape
     */
    private static final class ShapedRecipe {
        
        private final String[] ids;
        private final Shape shape;
        private final int hashCode;

        private ShapedRecipe(@Nonnull ItemStack[] items) {
            
            // map of ids to digit for shape
            Map<String, Integer> map = new TreeMap<>();
            
            // array of ids for 3x3 shaped recipes
            String[] ids = new String[9];
            
            // the shape int
            int shapeInt = 0;
            
            // the shape
            Shape shape = null;
            
            // the current digit
            AtomicInteger digit = new AtomicInteger(1);
            
            // find all ids and the shape int
            for (int i = 0; i < 9 ; i++) {
                shapeInt*=10;
                if (items[i] != null) {
                    shapeInt += map.computeIfAbsent(ids[i] = StackUtils.getIDorType(items[i]), k -> digit.getAndIncrement());
                    if (digit.intValue() == 4) {
                        // no shape has 4 different types rn
                        shape = Shape.FULL;
                        break;
                    }
                }
            }
    
            if (shape == null) {
                shape = Shape.MAP.getOrDefault(shapeInt, Shape.FULL);
            }
    
            int hashCode = 0;
            if (shape == Shape.FULL) {
                // use the array already made
                for (String id : ids) {
                    if (id != null) {
                        hashCode += id.hashCode();
                    }
                }
            } else {
                ids = new String[map.size()];
                if (shape == Shape.SHAPELESS) {
                    // sort tree map entries into array
                    int i = 0;
                    for (Map.Entry<String, Integer> entry : map.entrySet()) {
                        ids[i++] = entry.getKey();
                        hashCode += entry.getValue().hashCode();
                    }
                } else {
                    // just use the tree map entries
                    for (Map.Entry<String, Integer> entry : map.entrySet()) {
                        ids[entry.getValue() - 1] = entry.getKey();
                        hashCode += entry.getValue().hashCode();
                    }
                }
            }
            
            this.shape = shape;
            this.ids = ids;
            this.hashCode = hashCode + shape.hashCode();
        }
    
        @Override
        public final int hashCode() {
            return this.hashCode;
        }
    
        @Override
        public final boolean equals(Object obj) {
            if (!(obj instanceof ShapedRecipe)) {
                return false;
            }
            ShapedRecipe recipe = (ShapedRecipe) obj;
            if (recipe.shape != this.shape) {
                return false;
            }
            for (int i = 0 ; i < this.ids.length ; i++) {
                if (!Objects.equals(recipe.ids[i], this.ids[i])) {
                    return false;
                }
            }
            return true;
        }
    
        /**
         * Utility enum of 'shapes', for example a helmet could be:
         * 
         * 111    000
         * 101 or 111
         * 000    101
         *
         */
        private enum Shape {
            
            SHAPELESS(120000000, 123000000),
            
            SINGLE(100000000, 10000000, 1000000, 100000, 10000, 1000, 100, 10, 1), 
            
            TWO_UP(100100000, 10010000, 1001000, 100100, 10010, 1001),
            TWO_SIDE(110000000, 11000000, 110000, 11000, 110, 11),
            
            THREE_UP(100100100, 10010010, 1001001),
            THREE_SIDE(111000000, 111000, 111),
            THREE_SWORD(100100200, 10010020, 1001002),
            THREE_ARROW(100200300, 10020030, 1002003),
            THREE_SHOVEL(100200200, 10020020, 1002002),
            THREE_SOULBOUND(10020010, 100200100, 1002001),
            
            FOUR_SQUARE(110110000, 11011000, 110110, 11011),
            FOUR_HOE(110200200, 110020020, 11020020, 11002002),
            FOUR_BOOTS(101101000, 101101),
            
            FIVE_AXE(110120020, 11012002, 110210200, 11021020),
            FIVE_HELMET(111101000, 111101),
            
            SIX_TABLE(111111000, 111111),
            SIX_DOOR(110110110, 11011011),
            SIX_STAIRS(1011111, 100110111),
            SIX_BOW(120102120, 12102012),
            
            FULL();
            
            private final int[] shapes;
            
            Shape(int... shapes) {
                this.shapes = shapes;
            }
    
            private static final Map<Integer, Shape> MAP = new HashMap<>();
    
            static {
                for (Shape shape : Shape.values()) {
                    for (int i : shape.shapes) {
                        MAP.put(i, shape);
                    }
                }
            }
            
        }
    
    }

}
