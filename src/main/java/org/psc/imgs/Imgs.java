package org.psc.imgs;

import io.github.sndpg.imgs.ColorCombinations;
import io.github.sndpg.imgs.ProfileImageTemplate;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

public class Imgs {

    public static void main(String[] args) {
        BufferedImage psc = ProfileImageTemplate.INSTANCE.invoke("PSC", configurer -> {
            configurer.setColorCombination(ColorCombinations.Cyan.INSTANCE.get(0));
            return null;
        });

        ProfileImageTemplate.INSTANCE.then(
                psc,
                ProfileImageTemplate.INSTANCE.writeToFile(Path.of("M:\\some.png"), "PNG")
        );
    }
}
