package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.data.TransactionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Ink = Color(0xFFF2F5F3)
private val Green = Color(0xFF77D6A6)
private val Pale = Color(0xFF101512)
private val Surface = Color(0xFF1A211D)

class MainActivity : ComponentActivity() {
    private val viewModel: TransactionViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Green, onPrimary = Color(0xFF082316), background = Pale, surface = Surface, onSurface = Ink)) {
                PocketLedger(viewModel)
            }
        }
    }
}

@Composable
private fun PocketLedger(viewModel: TransactionViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    var tab by remember { mutableStateOf("Home") }
    var showAdd by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    Scaffold(containerColor = Pale, bottomBar = {
        NavigationBar(containerColor = Surface) {
            listOf("Home", "History", "Stats").forEach { item ->
                NavigationBarItem(selected = tab == item, onClick = { tab = item }, icon = { Text(when(item) { "Home" -> "⌂"; "History" -> "▤"; else -> "◔" }, fontSize = 20.sp) }, label = { Text(item) })
            }
        }
    }, floatingActionButton = {
        FloatingActionButton(onClick = { showAdd = true }, containerColor = Green, contentColor = Color.White) { Text("+", fontSize = 28.sp) }
    }) { padding ->
        when (tab) {
            "Home" -> Dashboard(transactions, onDelete = { transactionToDelete = it }, modifier = Modifier.padding(padding))
            "History" -> History(transactions, onDelete = { transactionToDelete = it }, modifier = Modifier.padding(padding))
            else -> Statistics(transactions, Modifier.padding(padding))
        }
    }
    if (showAdd) AddTransactionDialog(onDismiss = { showAdd = false }, onSave = { amount, type, category, payment, note ->
        viewModel.add(amount, type, category, payment, note); showAdd = false
    })
    transactionToDelete?.let { transaction ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Delete transaction?") },
            text = { Text("This will remove ${transaction.category} (${money(transaction.amount)}) from your records.") },
            confirmButton = { TextButton(onClick = { viewModel.delete(transaction); transactionToDelete = null }) { Text("Delete", color = Color(0xFFFF8A80)) } },
            dismissButton = { TextButton(onClick = { transactionToDelete = null }) { Text("Keep") } }
        )
    }
}

@Composable
private fun Dashboard(items: List<TransactionEntity>, onDelete: (TransactionEntity) -> Unit, modifier: Modifier = Modifier) {
    val income = items.filter { it.type == "INCOME" }.sumOf { it.amount }
    val expenses = items.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val monthStart = java.util.Calendar.getInstance().apply { set(java.util.Calendar.DAY_OF_MONTH, 1); set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0); set(java.util.Calendar.SECOND, 0) }.timeInMillis
    val monthSpend = items.filter { it.type == "EXPENSE" && it.dateMillis >= monthStart }.sumOf { it.amount }
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Image(painterResource(R.drawable.pocket_logo), contentDescription = "Pocket Ledger logo", modifier = Modifier.size(46.dp))
                Text("POCKET LEDGER", color = Green, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
            Text("Your money,\nin good hands.", color = Ink, fontSize = 31.sp, lineHeight = 37.sp, fontWeight = FontWeight.Bold)
        }
        item {
            Column(Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(24.dp)).padding(22.dp)) {
                Text("TOTAL BALANCE", color = Color(0xFF9AA79F), fontSize = 12.sp, letterSpacing = 1.sp)
                Text(money(income - expenses), color = Ink, fontSize = 34.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 20.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    BalanceMetric("INCOME", income, Color(0xFF83D7AC)); BalanceMetric("SPENT", expenses, Color(0xFFFFB29F))
                }
            }
        }
        item { Row(Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(18.dp)).padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column { Text("THIS MONTH", color = Color.Gray, fontSize = 11.sp, letterSpacing = 1.sp); Text("Spending", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) }
            Text(money(monthSpend), color = Green, fontSize = 21.sp, fontWeight = FontWeight.Bold)
        } }
        item { Text("Recent activity", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink) }
        if (items.isEmpty()) item { EmptyState() } else items.take(5).forEach { item { TransactionRow(it, onDelete) } }
    }
}

@Composable
private fun BalanceMetric(label: String, amount: Double, color: Color) {
    Column { Text(label, color = Color(0xFF9AA79F), fontSize = 11.sp); Text(money(amount), color = color, fontWeight = FontWeight.SemiBold, fontSize = 17.sp) }
}

@Composable
private fun History(items: List<TransactionEntity>, onDelete: (TransactionEntity) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(horizontal = 22.dp)) {
        Text("Activity", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.padding(top = 26.dp, bottom = 16.dp))
        if (items.isEmpty()) EmptyState() else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) { items(items) { TransactionRow(it, onDelete) } }
    }
}

@Composable
private fun Statistics(items: List<TransactionEntity>, modifier: Modifier = Modifier) {
    val expenses = items.filter { it.type == "EXPENSE" }
    val total = expenses.sumOf { it.amount }
    Column(modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Your stats", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Ink)
        Text("Spending by category", color = Color.Gray)
        if (expenses.isEmpty()) EmptyState() else expenses.groupBy { it.category }.entries.sortedByDescending { it.value.sumOf(TransactionEntity::amount) }.forEach { (category, rows) ->
            val value = rows.sumOf { it.amount }
            Column(Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(16.dp)).padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(category, color = Ink, fontWeight = FontWeight.SemiBold); Text(money(value), color = Ink) }
                LinearProgressIndicator(progress = { if (total == 0.0) 0f else (value / total).toFloat() }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp), color = Green, trackColor = Pale)
            }
        }
    }
}

@Composable
private fun TransactionRow(item: TransactionEntity, onDelete: (TransactionEntity) -> Unit) {
    val positive = item.type == "INCOME"
    var menuOpen by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(16.dp)).padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(42.dp).background(Pale, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Text(categoryIcon(item.category), fontSize = 20.sp) }
        Column(Modifier.weight(1f).padding(start = 12.dp)) { Text(item.category, color = Ink, fontWeight = FontWeight.SemiBold); Text(item.note.ifBlank { item.paymentMethod }, color = Color.Gray, fontSize = 12.sp) }
        Text((if (positive) "+" else "−") + money(item.amount), color = if (positive) Green else Ink, fontWeight = FontWeight.Bold)
        Box {
            TextButton(onClick = { menuOpen = true }, contentPadding = PaddingValues(start = 8.dp)) { Text("⋮", color = Color.Gray, fontSize = 20.sp) }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(text = { Text("Delete") }, onClick = { menuOpen = false; onDelete(item) })
            }
        }
    }
}

@Composable
private fun EmptyState() { Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("☀️", fontSize = 30.sp); Text("A fresh start", fontWeight = FontWeight.SemiBold, color = Ink); Text("Add your first transaction with +", color = Color.Gray, fontSize = 13.sp) } }

@Composable
private fun AddTransactionDialog(onDismiss: () -> Unit, onSave: (Double, String, String, String, String) -> Unit) {
    var type by remember { mutableStateOf("EXPENSE") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food") }
    var payment by remember { mutableStateOf("UPI") }
    var note by remember { mutableStateOf("") }
    val categories = if (type == "EXPENSE") listOf("Food", "Transport", "Shopping", "Entertainment", "Education", "Bills", "Health", "Other") else listOf("Salary", "Pocket money", "Freelance", "Other income")
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Add transaction", color = Ink) }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("EXPENSE", "INCOME").forEach { option -> FilterChip(selected = type == option, onClick = { type = option; category = if (option == "INCOME") "Salary" else "Food" }, label = { Text(option.lowercase().replaceFirstChar(Char::uppercase)) }) } }
            OutlinedTextField(amount, { amount = it }, label = { Text("Amount (₹)") }, singleLine = true)
            var expanded by remember { mutableStateOf(false) }
            Box { OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text("$category  ▾") }; DropdownMenu(expanded, { expanded = false }) { categories.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { category = it; expanded = false }) } } }
            OutlinedTextField(payment, { payment = it }, label = { Text("Payment method") }, singleLine = true)
            OutlinedTextField(note, { note = it }, label = { Text("Note") }, singleLine = true)
            Text("Date: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())}", color = Color.Gray, fontSize = 13.sp)
        }
    }, confirmButton = { TextButton(onClick = { amount.toDoubleOrNull()?.takeIf { it > 0 }?.let { onSave(it, type, category, payment, note) } }) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

private fun money(amount: Double) = "₹" + String.format(Locale("en", "IN"), "%,.2f", amount)
private fun categoryIcon(category: String) = when (category) { "Food" -> "🍲"; "Transport" -> "🚌"; "Shopping" -> "🛍️"; "Entertainment" -> "🎮"; "Education" -> "📚"; "Bills" -> "💡"; "Health" -> "🩺"; "Salary" -> "💼"; else -> "✦" }
