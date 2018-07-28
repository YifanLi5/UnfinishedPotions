package Nodes;

import Nodes.BankingNodes.DecideRestockNode;
import Nodes.BankingNodes.DepositNode;
import Nodes.CreationNodes.HoverBankerCreation;
import Nodes.GENodes.InitialBuy;
import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.CombinationRecipes;
import Util.Margins;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;

import java.util.List;

public class StartingNode implements ExecutableNode {
    private Script script;
    private Class<? extends ExecutableNode> jumpTarget;
    private Margins margins;

    public StartingNode(Script script) {
        this.script = script;
        margins = Margins.getInstance(script);
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        return true;
    }

    @Override
    public int executeNode() throws InterruptedException {
        Inventory inv = script.getInventory();
        if(script.getBank().open()){
            CombinationRecipes recipes = getPreStockedRecipe();
            margins.setCurrentRecipe(recipes);
            if(recipes == null){
                if(invContainsPrimaryComponent())
                    jumpTarget = DepositNode.class;
                else
                    jumpTarget = InitialBuy.class;
            } else if(invContainsPrimaryComponent() && inv.contains(recipes.getSecondaryItemName())){
                jumpTarget = HoverBankerCreation.class;
            } else if(inv.isEmpty()){
                jumpTarget = DecideRestockNode.class;
            } else {
                jumpTarget = DepositNode.class;
            }
        }

        return 0;
    }

    private boolean invContainsPrimaryComponent(){
        Inventory inv = script.getInventory();
        return inv.contains(CombinationRecipes.AVANTOE.getPrimaryItemName())
                || inv.contains(CombinationRecipes.TOADFLAX.getPrimaryItemName())
                || inv.contains(CombinationRecipes.RANARR.getPrimaryItemName())
                || inv.contains(CombinationRecipes.IRIT.getPrimaryItemName())
                || inv.contains(CombinationRecipes.HARRALANDER.getPrimaryItemName());
    }

    private CombinationRecipes getPreStockedRecipe(){
        Bank bank = script.getBank();
        CombinationRecipes todo = null;
        int herbLvl = script.skills.getDynamic(Skill.HERBLORE);
        for(CombinationRecipes recipe: CombinationRecipes.values()){
            if(recipe == CombinationRecipes.CLAY)
                continue;
            int amt = (int) (bank.getAmount(recipe.getPrimaryItemName()) + script.getInventory().getAmount(recipe.getPrimaryItemName()));
            if(amt > 14 && herbLvl >= recipe.getReqLvl()){
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
