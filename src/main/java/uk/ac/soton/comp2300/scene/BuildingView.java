package uk.ac.soton.comp2300.scene;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import uk.ac.soton.comp2300.model.game_logic.BuildingType;

import java.util.HashMap;
import java.util.Map;

import static uk.ac.soton.comp2300.model.Resource.*;

public class BuildingView {

    public static final Map<BuildingType, PhongMaterial> materialMap = new HashMap<>();
    static {
        materialMap.put(BuildingType.LUMBER_MILL, new PhongMaterial(Color.BROWN));
        materialMap.put(BuildingType.QUARRY, new PhongMaterial(Color.DARKGRAY));
        materialMap.put(BuildingType.TOWN, new PhongMaterial(Color.GOLD));
        materialMap.put(BuildingType.MINE, new PhongMaterial(Color.LIGHTBLUE));
        materialMap.put(BuildingType.RESEARCH_LAB, new PhongMaterial(Color.rgb(215, 255, 215)));
        materialMap.put(BuildingType.SPACEPORT, new PhongMaterial(Color.DARKCYAN));
        materialMap.put(BuildingType.MARKET, new PhongMaterial(Color.PURPLE));
    }

    public static Node createNode(BuildingType type) {
        Cylinder pip = new Cylinder(7, 2);
        PhongMaterial material = materialMap.getOrDefault(type, new PhongMaterial(Color.BLACK));
        pip.setMaterial(material);

        String rawName = type.name().toLowerCase();
        String[] parts = rawName.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        String buildingImgName = sb.toString() + ".png";

        ImageView buildingIcon = new javafx.scene.image.ImageView();
        try {
            var stream = BuildingView.class.getResourceAsStream("/images/" + buildingImgName);
            if (stream != null) {
                buildingIcon.setImage(new javafx.scene.image.Image(stream));
                buildingIcon.setFitWidth(9);
                buildingIcon.setFitHeight(9);
                buildingIcon.setTranslateY(2);
                buildingIcon.setTranslateX(-4.5);
                buildingIcon.setTranslateZ(-4.5);

                Rotate rotate = new Rotate(90, Rotate.X_AXIS);
                buildingIcon.getTransforms().add(rotate);

                Group group = new Group(pip, buildingIcon);
                return group;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pip;
    }
}

