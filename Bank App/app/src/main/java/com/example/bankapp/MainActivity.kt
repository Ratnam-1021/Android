package com.example.bankapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BankApp()
        }
    }
}



@Composable
fun BankApp() {
    val navController = rememberNavController()

    val accounts = remember {
        mutableStateListOf(
            BankAccount("Admin", 10000),
            BankAccount("User1", 5000),
            BankAccount("User2", 7000)
        )
    }

    var transactionMessage by remember { mutableStateOf("") }
    var showTransactionPopup by remember { mutableStateOf(false) }

    //Helper ()
    val onTransactionSuccess: (String) -> Unit = { message ->
        transactionMessage = message
        showTransactionPopup = true
        navController.popBackStack()
    }


    var currentUser by remember { mutableStateOf(accounts.first { it.username == "Admin" }) }

    NavHost(navController = navController, startDestination = "Get Started") {

        composable("Get Started") {
            WelcomeScreen(navController)
        }

        composable("Login") {
            LoginScreen(
                navController = navController,
                allAccounts = accounts,
                onLoginSuccess = { loggedInUser ->
                    currentUser = loggedInUser
                }
            )
        }

        composable("Dashboard") {
            DashboardScreen(navController=navController, bankAccount=currentUser,showSuccessPopup = showTransactionPopup,
                successMessage = transactionMessage,
                onDismissPopup = { showTransactionPopup = false} )
        }

        composable("Deposit") {
            DepositScreen(navController=navController, bankAccount = currentUser,onSuccess = onTransactionSuccess)

        }

        composable("Withdraw") {
            WithdrawScreen(navController, bankAccount = currentUser,onSuccess = onTransactionSuccess)
        }

        composable("Transaction History") {
            TransactionHistoryScreen(navController, bankAccount = currentUser)
        }

        composable("Transfer") {
                TransferScreen(
                navController = navController,
                sender = currentUser,
                allAccounts = accounts,
                onSuccess = onTransactionSuccess
            )
        }
    }
}

@Composable
fun WelcomeScreen(navController: NavController) {
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
        ){
        Text(
            text="Welcome to Bank App",
            fontSize=32.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = { navController.navigate("Login") }) {
            Text(text = "Get Started")
        }
    }
}



@Composable
fun LoginScreen(navController: NavController,allAccounts: List<BankAccount>, onLoginSuccess: (BankAccount) -> Unit) {
    val username = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val message = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Bank Login",
            fontSize = 32.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        TextField(
            value = username.value,
            onValueChange = { username.value = it },
            label = { Text("Username") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {

                val foundUser = allAccounts.find {
                    it.username.equals(username.value, ignoreCase = true)
                }

                if (foundUser != null && password.value == "1234") {
                    onLoginSuccess(foundUser)
                    navController.navigate("Dashboard") {
                        popUpTo("Login") { inclusive = true }
                    }
                } else {
                    message.value = "Invalid username or password"
                }
            }
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message.value,
            fontSize = 16.sp
        )
    }
}

@Composable
fun DashboardScreen(navController: NavController,bankAccount: BankAccount,showSuccessPopup: Boolean,successMessage: String, onDismissPopup: () -> Unit) {

    var showLogoutDialog by remember { mutableStateOf(false) }

    BackHandler {
        showLogoutDialog = true
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(text = "Logout") },
            text = { Text(text = "Are you sure you want to logout?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        navController.navigate("Login") {
                            popUpTo("Dashboard") { inclusive = true }
                        }
                    },
                    colors = buttonColors(containerColor = Color.Red)
                ) {
                    Text("Yes, Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSuccessPopup) {
        AlertDialog(
            onDismissRequest = { onDismissPopup() },
            title = { Text("Success ✅") },
            text = { Text(successMessage) },
            confirmButton = {
                Button(onClick = { onDismissPopup() }) {
                    Text("OK")
                }
            }
        )
    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(androidx.compose.foundation.layout.WindowInsets.systemBars)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ){

        Text(
            text = "Bank Dashboard",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
        ){
            Column(
                modifier = Modifier.padding(16.dp)
                    .fillMaxWidth()
                    .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
            ){

                Text(
                    "Acc. Holder Name:  ${bankAccount.username}",
                    fontSize = 20.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Balance: ₹ ${bankAccount.balance} ",
                    fontSize = 20.sp
                )

            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { navController.navigate("Deposit") }
            ) {
                Text("Deposit")
            }

            Button(
                onClick = { navController.navigate("Withdraw") }
                    //bankAccount.balance -= 500
            ) {
                Text("Withdraw")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { navController.navigate("Transaction History") }
        ){
            Text("Transaction History")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { navController.navigate("Transfer") }
        ){
            Text("Transfer")
        }

    }
}

@Composable
fun DepositScreen(navController: NavController,bankAccount: BankAccount,onSuccess: (String) -> Unit) {

    var amount by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Text(
            text = "Deposit Money",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        TextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val amountValue = amount.toIntOrNull()
                if (amountValue != null && amountValue > 0) {
                    bankAccount.balance += amountValue
                    bankAccount.transactions.add(
                        Transaction("Deposit", amountValue, getCurrentDate())
                    )
                    onSuccess("Successfully deposited ₹$amountValue")
                } else {
                    message = "Invalid amount"
                }
            }
        ){
            Text("Deposit")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            fontSize = 16.sp
        )
    }
}

@Composable
fun WithdrawScreen(navController: NavController,bankAccount: BankAccount,onSuccess: (String) -> Unit) {

    var amount by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Text(
            text = "Withdraw Money",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        TextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val amountValue = amount.toIntOrNull()
                if (amountValue != null && amountValue > 0) {
                    if(amountValue <= bankAccount.balance) {
                        bankAccount.balance -= amountValue
                        bankAccount.transactions.add(Transaction("Withdraw", amountValue,getCurrentDate()))
                        onSuccess("Successfully withdrawn ₹$amountValue")
                    }
                    else{
                        message = "Insufficient balance"
                    }
                } else {
                    message = "Invalid amount"
                }
            }
        ){
            Text("Withdraw")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            fontSize = 16.sp
        )
    }
}




@Composable
fun TransactionHistoryScreen(navController: NavController, bankAccount: BankAccount) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(androidx.compose.foundation.layout.WindowInsets.systemBars)
            .padding(20.dp)
    ) {

        Text(
            text = "Transaction History",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (bankAccount.transactions.isEmpty()) {
            Text("No transactions yet", fontSize = 18.sp, color = Color.Gray)
        } else {
            LazyColumn {
                items(bankAccount.transactions) { transaction ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 12.dp,
                            bottom = 12.dp
                        ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 12.dp,
                                    bottom = 12.dp
                                )
                        ) {

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = transaction.type,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )

                                val isMoneyIn =
                                    transaction.type == "Deposit" || transaction.type == "Transfer Received"

                                val transactionColor = if (isMoneyIn) {
                                    Color(0xFF006400)
                                } else {
                                    Color.Red
                                }

                                Text(
                                    text = "₹ ${transaction.amount}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = transactionColor
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))


                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = transaction.date,
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "Balance: ₹ ${bankAccount.balance}",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransferScreen(navController: NavController, sender: BankAccount, allAccounts: List<BankAccount>,onSuccess: (String) -> Unit) {
    var receiverUsername by remember { mutableStateOf("") }
    var amountString by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Transfer Money", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(32.dp))


        TextField(
            value = receiverUsername,
            onValueChange = { receiverUsername = it },
            label = { Text("Receiver Username (e.g., user1)") }
        )

        Spacer(modifier = Modifier.height(16.dp))


        TextField(
            value = amountString,
            onValueChange = { amountString = it },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val amount = amountString.toIntOrNull()
                val receiver = allAccounts.find { it.username == receiverUsername }

                when {
                    amount == null || amount <= 0 ->
                        message = "Invalid amount"

                    receiver == null ->
                        message = "User '$receiverUsername' not found"

                    receiver.username == sender.username ->
                        message = "Cannot transfer to yourself"

                    sender.balance < amount ->
                        message = "Insufficient balance"

                    else -> {

                        sender.balance -= amount
                        receiver.balance += amount

                        sender.transactions.add(
                            0,
                            Transaction(
                                "Transferred to ${receiver.username}",
                                amount,
                                getCurrentDate()
                            )
                        )
                        receiver.transactions.add(
                            0,
                            Transaction("Transfer Received", amount, getCurrentDate())
                        )

                        onSuccess("Transferred ₹$amount to ${receiver.username}")
                    }
                }
            }
        ) {
            Text("Transfer")
        }
    }
}



//@Preview(showBackground = true)
//@Composable
//fun BankAppPreview() {
//    BankAppTheme {
//        BankApp()
//    }
//}
