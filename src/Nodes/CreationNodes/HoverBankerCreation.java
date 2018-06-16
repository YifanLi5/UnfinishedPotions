package Nodes.CreationNodes;

import Util.Statics;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.Menu;
import org.osbot.rs07.api.Mouse;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Option;
import org.osbot.rs07.input.mouse.EntityDestination;
import org.osbot.rs07.input.mouse.RectangleDestination;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.List;

public class HoverBankerCreation extends AbstractCreationNode {
    public HoverBankerCreation(Script script) {
        super(script);
    }

    @Override
    int waitForPotions() throws InterruptedException {

        boolean hovered = hoverOverBankOption();
        Inventory inv = script.getInventory();
        new ConditionalSleep(25000) {
            @Override
            public boolean condition() throws InterruptedException {
                return !inv.contains(recipe.getPrimaryItemName()) || !inv.contains(recipe.getSecondaryItemName());
            }
        }.sleep();

        if(hovered){
            Statics.shortRandomNormalDelay();
            script.getMouse().click(false);
        }
        return (int) Statics.randomNormalDist(1200, 200);
    }

    boolean hoverOverBankOption(){
        NPC banker = script.getNpcs().closest("Banker");
        Mouse mouse = script.getMouse();
        Menu menu = script.getMenuAPI();
        boolean success = false;
        if(banker != null){
            boolean found = false;
            int idx = 0;
            int attempts = 0;
            while(!found && attempts++ < 5){
                if(mouse.click(new EntityDestination(script.getBot(), banker), true)){
                    if(menu.isOpen()){
                        List<Option> options = menu.getMenu();
                        for(; idx < options.size(); idx++){
                            if(options.get(idx).action.equals("Bank")){
                                found = true;
                                break;
                            }
                        }
                    }
                }
            }
            if(found){
                RectangleDestination bankOptionRect = new RectangleDestination(script.getBot(), menu.getOptionRectangle(idx));
                success = mouse.move(bankOptionRect);
            }

        }
        return success;
    }
}
