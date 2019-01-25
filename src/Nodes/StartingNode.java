package Nodes;

import Nodes.BankingNodes.DecideRestockNode;
import Nodes.BankingNodes.DepositNode;
import Nodes.CreationNodes.HoverBankerCreation;
import Nodes.GENodes.InitialBuyWaitUntil;
import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.CombinationRecipes;
import Util.ItemData;
import Util.Margins;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.MethodProvider;

import java.util.List;

public class StartingNode extends MethodProvider implements ExecutableNode {
    private Class<? extends ExecutableNode> jumpTarget;
    private Margins margins;

    public StartingNode(Bot bot) {
        margins = Margins.getInstance(bot);
        exchangeContext(bot);
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        return true;
    }

    @Override
    public int executeNode() throws InterruptedException {
        if(bank.open()){
            CombinationRecipes recipe = findStartingRecipe();
            margins.setCurrentRecipe(recipe);
            if(recipe == null){
                if(invContainsPrimaryComponent())
                    jumpTarget = DepositNode.class;
                else
                    jumpTarget = InitialBuyWaitUntil.class;
            } else if(invContainsPrimaryComponent() && inventory.contains(recipe.getSecondary())){
                jumpTarget = HoverBankerCreation.class;
            } else if(inventory.isEmpty()){
                jumpTarget = DecideRestockNode.class;
            } else {
                jumpTarget = DepositNode.class;
            }
        }

        return 0;
    }

    private boolean invContainsPrimaryComponent(){
        return inventory.contains(ItemData.CLEAN_AVANTOE)
                || inventory.contains(ItemData.CLEAN_TOADFLAX)
                || inventory.contains(ItemData.CLEAN_IRIT)
                || inventory.contains(ItemData.CLEAN_KWUARM)
                || inventory.contains(ItemData.CLEAN_HARRALANDER);
    }

    private CombinationRecipes findStartingRecipe(){
        CombinationRecipes todo = null;
        int herbLvl = skills.getDynamic(Skill.HERBLORE);
        for(CombinationRecipes recipe: CombinationRecipes.values()){
            int amountCreatable = (int) (bank.getAmount(recipe.getPrimary()) + inventory.getAmount(recipe.getPrimary()));
            if(amountCreatable > 14 && herbLvl >= recipe.getReqLvl()){
                todo = recipe;
            }
        }
        return todo;
    }

    @Override
    public List<Edge> getAdjacentNodes() {
        return null;
    }

    @Override
    public boolean isJumping() {
        return true;
    }

    @Override
    public Class<? extends ExecutableNode> setJumpTarget() {
        return jumpTarget;
    }

    @Override
    public void logNode() {
        this.getClass().getSimpleName();
    }
}
