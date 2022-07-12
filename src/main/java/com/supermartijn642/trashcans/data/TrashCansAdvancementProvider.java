package com.supermartijn642.trashcans.data;

import com.google.common.collect.Sets;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.trashcans.TrashCans;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.data.event.GatherDataEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Created 9/2/2021 by SuperMartijn642
 */
public class TrashCansAdvancementProvider implements DataProvider {

    private static final Logger LOGGER = LogManager.getLogger();
    private final DataGenerator generator;

    public TrashCansAdvancementProvider(GatherDataEvent e){
        this.generator = e.getGenerator();
    }

    @Override
    public void run(CachedOutput hashCache){
        Path path = this.generator.getOutputFolder();
        Set<ResourceLocation> set = Sets.newHashSet();
        Consumer<Advancement> consumer = (advancement) -> {
            if(!set.add(advancement.getId())){
                throw new IllegalStateException("Duplicate advancement " + advancement.getId());
            }else{
                Path advancementPath = createPath(path, advancement);
                try{
                    DataProvider.saveStable(hashCache, advancement.deconstruct().serializeToJson(), advancementPath);
                }catch(IOException ioexception){
                    LOGGER.error("Couldn't save advancement {}", advancementPath, ioexception);
                }
            }
        };

        Advancement trash_can = Advancement.Builder.advancement()
            .display(TrashCans.item_trash_can.asItem(), TextComponents.translation("trashcans.advancement.trash_can.title").get(), TextComponents.translation("trashcans.advancement.trash_can.description").get(), new ResourceLocation("minecraft", "textures/block/cobblestone.png"), FrameType.TASK, true, true, false)
            .requirements(RequirementsStrategy.OR)
            .addCriterion("has_trash_can", InventoryChangeTrigger.TriggerInstance.hasItems(TrashCans.item_trash_can))
            .addCriterion("has_trash_can2", InventoryChangeTrigger.TriggerInstance.hasItems(TrashCans.liquid_trash_can))
            .addCriterion("has_trash_can3", InventoryChangeTrigger.TriggerInstance.hasItems(TrashCans.energy_trash_can))
            .save(consumer, "trashcans:trash_can");
        Advancement ultimate_trash_can = Advancement.Builder.advancement()
            .parent(trash_can)
            .display(TrashCans.ultimate_trash_can.asItem(), TextComponents.translation("trashcans.advancement.ultimate_trash_can.title").get(), TextComponents.translation("trashcans.advancement.ultimate_trash_can.description").get(), null, FrameType.TASK, true, true, false)
            .addCriterion("has_trash_can", InventoryChangeTrigger.TriggerInstance.hasItems(TrashCans.ultimate_trash_can))
            .save(consumer, "trashcans:ultimate_trash_can");
    }

    private static Path createPath(Path path, Advancement advancement){
        return path.resolve("data/" + advancement.getId().getNamespace() + "/advancements/" + advancement.getId().getPath() + ".json");
    }

    public String getName(){
        return "Advancements";
    }
}
