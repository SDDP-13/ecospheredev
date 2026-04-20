package uk.ac.soton.comp2300.model;

import java.util.Map;

import uk.ac.soton.comp2300.App;
import uk.ac.soton.comp2300.model.game_logic.Planet;

public enum Items {

        PICKAXE(Map.of(Resource.STONE, 1.03),0, 1, "Stone production increased by 3%"),
        DYNAMITE(Map.of(Resource.STONE, 1.06),0, 2, "Stone production increased by 6%"),
        GOLD_BAR(Map.of(Resource.MONEY, 1.03),0, 3 , "Gold production increased by 3%"),
        DIAMONDS(Map.of(Resource.MONEY, 1.04),0, 4, "Gold production increased by 4%"),
        CHAINSAW(Map.of(Resource.WOOD, 1.03),0, 5, "Wood production increased by 3%"),
        SAWMILL(Map.of(Resource.WOOD, 1.07),0, 6, "Wood production increased by 7%"),
        BELLOWS(Map.of(Resource.METAL, 1.03),0, 7, "Metal production increased by 3%" ),
        FORGE(Map.of(Resource.METAL, 1.05),0, 8, "Metal production increased by 5%"),
        QUANTUM_COMPUTING (Map.of(), 500,9, "Xp bonus!");

        private final Map<Resource, Double> multipliers;
        private final int xpGain;
        private final int rewardLvl;
        private final String message;
        private static final int rewardModulo = 10;

        Items(Map<Resource, Double> multiplier, int xpGain, int rewardLvl, String message) {
            this.multipliers = multiplier;
            this.xpGain = xpGain;
            this.rewardLvl = rewardLvl;
            this.message = message;
        }

        public Map<Resource, Double> getMultipliers() {
            return multipliers;
        }


        public boolean quantumComp(){
            return this == QUANTUM_COMPUTING;
        }

        public int getXpGain(){
            return xpGain;
        }
        public String getMessage() {return message;}
        public int getRewardLvl() {return rewardLvl; }

        public boolean increaseXp(){
            return xpGain > 0;
        }

        public static int selectRewardLvl(int lvl) {
            int rewardSelect = lvl % rewardModulo;
            return rewardSelect;
        }

        public static Items selectItem(int rewardSelector) {
            for (Items item : Items.values()) {
                if (item.rewardLvl == rewardSelector) {
                    return item;
                }
            }
            throw new IllegalStateException("No item matches reward level " + rewardSelector);
        }

        public void applyItem(Planet planet) {
            if (this.increaseXp()) {
                App.getInstance().addXp(this.xpGain);
            }else {
                for (Map.Entry<Resource, Double> resEntry : this.getMultipliers().entrySet()) {
                    planet.changeMultiplier(resEntry.getKey(), resEntry.getValue());
                }
            }
        }





}


