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
        materialMap.put(BuildingType.RESEARCH_LAB, new PhongMaterial(Color.WHITE));
        materialMap.put(BuildingType.SPACEPORT, new PhongMaterial(Color.DARKCYAN));
        materialMap.put(BuildingType.MARKET, new PhongMaterial(Color.PURPLE));
    }

    public static Node createNode(BuildingType type) {
        Cylinder pip = new Cylinder(7, 2);
        PhongMaterial material = materialMap.getOrDefault(type, new PhongMaterial(Color.BLACK));
        pip.setMaterial(material);


        String iconPath = "/images/" + type.name().toLowerCase().replace("_", "") + ".png";
        try {
            Image iconImage = new Image(BuildingView.class.getResourceAsStream(iconPath));

            ImageView imageView = new ImageView(iconImage);
            imageView.setFitWidth(9);
            imageView.setFitHeight(9);
            imageView.setTranslateY(2);
            imageView.setTranslateX(-4.5);
            imageView.setTranslateZ(-4.5);

            Rotate rotate = new Rotate(90, Rotate.X_AXIS);
            imageView.getTransforms().add(rotate);

            Group group = new Group(pip, imageView);
            return group;

        } catch (Exception ignored) {}
        return pip;
    }
}

