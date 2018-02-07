package ScriptClasses;

import Nodes.BankUNFPotionsNode;
import Nodes.CreateUNFPotionsNode;
import Nodes.ExecutableNode;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(author = "PayPalMeRSGP", name = "bank1", info = "goldfarming unf potion mater", version = 0.1, logo = "")
public class MainScript extends Script{

    GraphBasedNodeExecutor executor;
    @Override
    public void onStart() throws InterruptedException {
        super.onStart();
        ExecutableNode create = CreateUNFPotionsNode.getInstance(this);
        ExecutableNode bank = BankUNFPotionsNode.getInstance(this);

        executor = new GraphBasedNodeExecutor(bank);
        executor.addEdgeToNode(bank, create, 1);
        executor.addEdgeToNode(create, bank, 1);
    }

    @Override
    public int onLoop() throws InterruptedException {
        return executor.executeNodeThenTraverse();
    }
}
