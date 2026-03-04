package uk.ac.soton.comp2300.scene;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import uk.ac.soton.comp2300.model.game_logic.BuildingType;

import java.util.HashMap;
import java.util.Map;

public class BuildingView {

    public static final Map<BuildingType, PhongMaterial> materialMap = new HashMap<>();
    static {
        materialMap.put(BuildingType.LUMBER_MILL, new PhongMaterial(Color.BROWN));
        materialMap.put(BuildingType.QUARRY, new PhongMaterial(Color.LIGHTGRAY));
        materialMap.put(BuildingType.TOWN, new PhongMaterial(Color.GOLD));
        materialMap.put(BuildingType.MINE, new PhongMaterial(Color.LIGHTBLUE));
        materialMap.put(BuildingType.RESEARCH_LAB, new PhongMaterial(Color.WHITE));
        materialMap.put(BuildingType.SPACEPORT, new PhongMaterial(Color.DARKCYAN));
        materialMap.put(BuildingType.MARKET, new PhongMaterial(Color.PURPLE));
    }

    public static Node createNode(BuildingType type) {
        Cylinder pip = new Cylinder(7, 2);
        PhongMaterial material = materialMap.getOrDefault(type, new PhongMaterial(Color.BLACK));
        pip.setMaterial(material);
        return pip;
    }
}

