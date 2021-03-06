package mini.twitter;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.ArrayList;

/**
 * File:
 * Goal: To Define the Admin Panel GUI
 * */


public class AdminControlPanel extends Application
{
    /**Private Widget Data Fields */

    private TreeView<UserComponent> tree; //Used to Show Users and UserGroups
    private TreeItem<UserComponent> root; //Root Of the tree

    private TextArea userIdTextArea;
    private Button addUserButton;

    private TextArea groupIdTextArea;
    private Button addGroupButton;

    private Button openUserViewButton;

    private Button showUserTotalButton;
    private Button showMessageTotalButton;
    private Button showGroupTotalButton;
    private Button showPositivePercentageButton;

    private Button areIdsValidButton; //Used to Determine if all the IDS used in the users and groups are valid

    private Button lastUpdatedUserButton; //Used to Get the Last Updated User

    /**Private Data Fields*/
    private TreeItem<UserComponent> currentSelectedUserGroup; //Holds the Currently Selected UserGroup
    private TreeItem<UserComponent> currentSelectedUser; //Holds the Currently Selected User Initially Null

    private AdminControlPanelSingleton adminSingleton = AdminControlPanelSingleton.getInstance(); // Getting a reference to the singleton

    private SystemVisitor visitor;

    /**Private Static Final Fields */
    private static final String USER_GROUP = "**";

    //Default Constructor
    public AdminControlPanel()
    {
        //Initializing Private Data Fields
        String rootName = USER_GROUP + "Root" + USER_GROUP;
        this.root = new TreeItem<UserComponent>(new UserGroup(rootName));
        this.root.setExpanded(true);
        this.tree = new TreeView(root);
        tree.setShowRoot(true);
        this.userIdTextArea = new TextArea();
        this.addUserButton = new Button("Add User");
        this.groupIdTextArea = new TextArea();
        this.addGroupButton = new Button("Add Group");
        this.openUserViewButton = new Button("Open Selected User Panel");
        this.showUserTotalButton = new Button("Show User Total");
        this.showMessageTotalButton = new Button("Show Message Total");
        this.showGroupTotalButton = new Button("Show Group Total");
        this.showPositivePercentageButton = new Button("Show Positive Percentage");
        this.areIdsValidButton = new Button("Validate IDS");
        this.lastUpdatedUserButton = new Button("Get Last Updated User");

        //Setting the Size of the Private Widgets
        this.tree.setPrefSize(300,400);
        this.userIdTextArea.setPrefSize(200,50);
        this.groupIdTextArea.setPrefSize(200,50);

        //Initially The Currently Selected Item is the Root
        //It When No Item is Selected it will default to the Root
        this.currentSelectedUserGroup = this.root;
        this.currentSelectedUser = null; //Initially Null

        //Adding the prompts for the Text Areas
        this.userIdTextArea.setPromptText("Enter User To Add");
        this.groupIdTextArea.setPromptText("Enter Group To Add ");

        //Setting the Size of all Widgets in the layout
        this.setWidgetSize();

        this.visitor = new SystemVisitor();
    }


    /**
     * JavaFX Application Start Function Which starts the Admin Panel
     * - Defining GUI Stuff in this Function
     * */
    @Override
    public void start(Stage stage) throws Exception
    {
        Pane layout = new Pane(); //Defining the Layout Of the Stage
        //Adding all the Widgets to the Layout
        layout.getChildren().addAll(tree, userIdTextArea, addUserButton, groupIdTextArea, addGroupButton,
                openUserViewButton, showUserTotalButton, showMessageTotalButton, showGroupTotalButton, showPositivePercentageButton, areIdsValidButton,
                lastUpdatedUserButton);

        //Positioning All the Widgets in the layout
        this.positionWidgets();

        //Checking if the user has selected another Tree Item In the Tree View
        this.tree.getSelectionModel().selectedItemProperty()
                .addListener((v, oldValue, newValue) -> {

                    //Checking if the User Has Selected another Tree Item By Default We assume its the Root
                    if(newValue != null)
                    {
                        //Ensuring the Newly selected tree item is a UserGroup
                        if(newValue.getValue() instanceof UserGroup)
                        {
                            this.currentSelectedUserGroup = newValue;//Updating the currently Selected TreeItem
                        }
                        else
                        {
                            this.currentSelectedUser = newValue; //Currently Selected User
                        }
                    }
                });

        /**Checking If Any Of the Buttons were Pressed*/

        //Checking if the Add User Button was pressed
        addUserButton.setOnAction(e -> {
            //Getting the User to add from the userIdTextArea
            String userToAdd = this.userIdTextArea.getText();
            //Trying to add the User to the program
            boolean isAdded = this.adminSingleton.addUser(userToAdd);
            //Added the User To the program
            if(isAdded)
            {
                //Updating our tree view
                this.makeUserBranch(userToAdd);
                //Clearing the userIdTextArea
                this.userIdTextArea.setText("");
            }
            //Did not add the user to the program
            else
            {
                //User Was Not Added Therefore Display a AlertBox
                AlertBox.display("AlertBox", "Error User Is Already Added\nPlease Try To Add A Unique User" );
            }
        });

        //Checking if the Add User Group Button Was Pressed
        addGroupButton.setOnAction(e ->{
            //Getting the User Group to add from the groupIdTextArea
            String userGroupToAdd = this.groupIdTextArea.getText();
            //Trying to add the UserGroup to the program
            boolean isAdded = this.adminSingleton.addUserGroup(userGroupToAdd);
            //Added the User Group to the Program
            if(isAdded)
            {
                //Updating our Tree View Since we have a Valid User to Add
               this.makeUserGroupBranch(userGroupToAdd);
               //Clearing the GroupIdTextArea
                this.groupIdTextArea.setText("");
            }
            //Did Not Add the User Group to the program
            else
            {
                //User Group was Not added to the Program
                AlertBox.display("AlertBox", "Error User Group Is Already Added\nPlease Try To Add A Unique User Group ");
            }
        });

        //Checking if the openUserViewButton was pressed
        openUserViewButton.setOnAction(e-> {
            //First Checking if the currentSelectedUser is valid
            if(this.currentSelectedUser != null)
            {
                //Starting the userControl panel Specified
                UserControlPanel panel = new UserControlPanel((User)this.currentSelectedUser.getValue());
                //Saving the UserControlPanel in the Singleton Using the name as the key
                this.adminSingleton.setUserControlPanels(this.currentSelectedUser.getValue().getName(), panel);
                panel.start(); //Starting the Panel

            }
            else
            {
                //Not Valid
                AlertBox.display("AlertBox", "Please Select A User\nIn Order to go to the User Control Panel");
            }

        });

        //Checking if the Show User Total Button Was Pressed
        showUserTotalButton.setOnAction(e ->{
            User temp = new User("");
            AlertBox.display("AlertBox", Integer.toString((int)temp.accept(this.visitor)));
        });

        //Checking if the Show Group Total Button was pressed
        showGroupTotalButton.setOnAction(e->{
            UserGroup temp = new UserGroup("");
            AlertBox.display("AlertBox", Integer.toString((int)temp.accept(this.visitor)));
        });

        //Checking if the Show Message Total Button Was Pressed
        showMessageTotalButton.setOnAction(e->{
            AlertBox.display("AlertBox", Integer.toString((int)visitor.visit(adminSingleton.getMessages())));
        });

        //Checking if the Show Positive Percentage Button Was Pressed
        showPositivePercentageButton.setOnAction(e->{
            AlertBox.display("AlertBox", Double.toString(visitor.visit()) + "%");
        });

        //Checking if The areIdsValidButton was Pressed
        areIdsValidButton.setOnAction(e -> {
            boolean isValid = this.validateIDS(); //Validates All the IDS of the Users and Groups

            if(isValid)
            {
                AlertBox.display("AlertBox" , "All Users and User Group IDS Are Valid ");
            }
            else
            {
                AlertBox.display("AlertBox", "\t\tError:\n\nUsers Or User Group Ids Are Not Valid\n\n\t***They Contain Spaces***");
            }

        });

        //Checking if the Last Updated User Button Was Pressed
        lastUpdatedUserButton.setOnAction(e ->{
            User lastUpdatedUser = adminSingleton.getLastUpdatedUser();
            if(lastUpdatedUser == null)
            {
                AlertBox.display("AlertBox", "Error: No User Has Made an Update");
            }
            else
            {
                AlertBox.display("AlertBox", "Last Updated User: " + lastUpdatedUser.getName());
            }

        });

        Scene scene = new Scene(layout, 800, 450);
        stage.setTitle("Admin Panel");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Validates All the IDS of the Users and User Groups Are Returns True if they are ALL Valid
     * Else Returns False if One or More are Invalid
     *
     * */
    private boolean validateIDS()
    {
        /**Getting all the Users and User Groups Currently In the Program */
        ArrayList<User> users = this.adminSingleton.getUsers();
        ArrayList<UserGroup> userGroups = this.adminSingleton.getUserGroups();
        /**Since we Already Validate If the User / UserGroups Are duplicate when adding them */
        /**All We have to check for is if their IDS Contain Spaces*/


        //Checking if Any User Contains a ID with a Space
        for(User u : users)
        {
            //If it contains a Space
            if(u.getName().contains(" "))
            {
                return false; //Not Valid
            }
        }
        //Checking if Any User Group Contains a ID with a Space
        for(UserGroup ug : userGroups)
        {
            if(ug.getName().contains(" "))
            {
                return false; //Not Valid
            }
        }


        return true; //VALID
    }



    /**
     * Private Helper Method Which Adds Users To the Tree View
     * - Adds the User based on the currentSelectedTreeItem
     * - Which defaults to root if the user hasn't selected other Groups
     * */
    private void makeUserBranch(String name)
    {
        //Getting the User From the AdminSingleton
        User user = this.adminSingleton.getUser(name);
        TreeItem<UserComponent> userToAdd = new TreeItem<UserComponent>(user);
        userToAdd.setExpanded(true);
        this.currentSelectedUserGroup.getChildren().add(userToAdd);
    }

    /**
     * Private Helper Method which
     * - Adds User groups to the Tree View
     * - Adds the User Group to the Tree View Based on the currentSelectedTreeItem
     * - Which defaults to root if the user hasn't selected other Groups
     * */
    private void makeUserGroupBranch(String userGroupName)
    {
        String newUserGroupName = USER_GROUP + userGroupName + USER_GROUP;
        TreeItem<UserComponent> userGroupToAdd = new TreeItem<>(new UserGroup(newUserGroupName));
        userGroupToAdd.setExpanded(true);

        //Adding the UserGroup as a child to the currently selected Tree Item
        this.currentSelectedUserGroup.getChildren().add(userGroupToAdd);
    }

    //Positioning all the widgets
    private void positionWidgets()
    {
        tree.setLayoutX(0);
        tree.setLayoutY(0);

        userIdTextArea.setLayoutX(400);
        userIdTextArea.setLayoutY(0);
        addUserButton.setLayoutX(600);
        addUserButton.setLayoutY(0);

        groupIdTextArea.setLayoutX(400);
        groupIdTextArea.setLayoutY(50);
        addGroupButton.setLayoutX(600);
        addGroupButton.setLayoutY(50);

        openUserViewButton.setLayoutX(400);
        openUserViewButton.setLayoutY(150);

        showUserTotalButton.setLayoutX(400);
        showUserTotalButton.setLayoutY(250);

        showGroupTotalButton.setLayoutX(550);
        showGroupTotalButton.setLayoutY(250);

        showMessageTotalButton.setLayoutX(400);
        showMessageTotalButton.setLayoutY(300);

        showPositivePercentageButton.setLayoutX(550);
        showPositivePercentageButton.setLayoutY(300);

        areIdsValidButton.setLayoutX(400);
        areIdsValidButton.setLayoutY(350);

        lastUpdatedUserButton.setLayoutX(550);
        lastUpdatedUserButton.setLayoutY(350);

    }
    /**
     * Defines the Size Of all the Widgets in the Layout
     * */
    private void setWidgetSize()
    {
        addUserButton.setMinSize(100, 50);
        addGroupButton.setMinSize(100,50);
        openUserViewButton.setMinSize(300,50);
        showUserTotalButton.setMinSize(150,50);
        showGroupTotalButton.setMinSize(150,50);
        showMessageTotalButton.setMinSize(150,50);
        showPositivePercentageButton.setMinSize(150,50);
        areIdsValidButton.setMinSize(150,50);
        lastUpdatedUserButton.setMinSize(150, 50);

    }
}
