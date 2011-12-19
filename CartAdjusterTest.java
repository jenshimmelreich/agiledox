package de.app.cart;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.product.ProductModel;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import de.Order;


@UnitTest
public class CartAdjusterTest {
	private static final BigDecimal TMP_ORDER_NUMBER = new BigDecimal(123);
	private static final BigDecimal CUSTOMER_NUMBER = new BigDecimal(456);
	private static final BigDecimal CREATION_DATE = new BigDecimal(20110930);
	private static final BigDecimal CREATION_TIME = new BigDecimal(221504);
	private static final BigDecimal TEXT_KEY = new BigDecimal(9999);
	private static final String UNIT = "Stueck";
	private static final String DESCRIPTION = "Description";
	private static final BigDecimal LINE_TEXT_KEY = new BigDecimal(123);
	private static final int LINE_NUMBER = 1;
	private static final String ARTICLE_NUMBER = "DefaultArticleNumber";
	private static final long QUANTITY = 1l;
	private static final double PRICE = 1.50;

	private CartModel cart;
	private CartAdjuster adjuster;
	private ResultSet resultSet;
	private HybrisCartHandler cartHandler;

	public void createCart() {
		cart = new CartModel();
		cart.setEntries(new ArrayList<AbstractOrderEntryModel>());
		cartHandler = mock(HybrisCartHandler.class);
		when(cartHandler.getCart()).thenReturn(cart);
	}

	public void prepareOrder() throws Exception {
		resultSet = mock(ResultSet.class);
		when(resultSet.isFirst()).thenReturn(true, false);
		when(resultSet.next()).thenReturn(false);
		when(resultSet.getBigDecimal("XXXXXXX")).thenReturn(TMP_ORDER_NUMBER);
		when(resultSet.getBigDecimal("XXXXXXX")).thenReturn(CUSTOMER_NUMBER);
		when(resultSet.getBigDecimal("XXXXXXX")).thenReturn(TEXT_KEY);
		when(resultSet.getBigDecimal("XXXXXXX")).thenReturn(CREATION_DATE);
		when(resultSet.getBigDecimal("XXXXXXX")).thenReturn(CREATION_TIME);
		when(resultSet.getString("XXXXXXX")).thenReturn(UNIT);
		when(resultSet.getString("XXXXXXX")).thenReturn(DESCRIPTION);
		when(resultSet.getBigDecimal("XXXXXXX")).thenReturn(LINE_TEXT_KEY);
		when(resultSet.getBigDecimal("XXXXXXX")).thenReturn(TMP_ORDER_NUMBER);
		when(resultSet.getBigDecimal("XXXXXXX")).thenReturn(new BigDecimal(LINE_NUMBER));
		when(resultSet.getString("XXXXXXX")).thenReturn(ARTICLE_NUMBER);
		when(resultSet.getBigDecimal("XXXXXXX")).thenReturn(new BigDecimal(QUANTITY));
		when(resultSet.getBigDecimal("XXXXXXX")).thenReturn(new BigDecimal(PRICE));
	}

	@Before
	public void setupTest() throws Exception {
		createCart();
		prepareOrder();
		adjuster = new CartAdjuster(cartHandler);
	}

	@Test
	public void shouldDoNothingIfCartAndOrderHaveNoItemsAndThereIsNoOrder() throws Exception {
		adjuster.correctBy(order());
	}

	@Test
	public void shouldCorrectThePriceOfAnEntry() throws Exception {
		CartEntryModel entry = createCartEntry(LINE_NUMBER, PRICE + 1.00, QUANTITY, ARTICLE_NUMBER);
		when(resultSet.next()).thenReturn(true, false);

		adjuster.correctBy(order());
		
		assertEquals(PRICE, entry.getTotalPrice(), 0.1);
		verify(cartHandler).saveCartEntry(entry);
	}

	@Test
	public void shouldCorrectThePriceOfMoreThanOneEntry() throws Exception {
		CartEntryModel entry1 = createCartEntry(1, PRICE + 10.00, QUANTITY, ARTICLE_NUMBER);
		CartEntryModel entry2 = createCartEntry(2, PRICE + 10.00, QUANTITY, ARTICLE_NUMBER);
		when(resultSet.getBigDecimal("XXXXXXX")).thenReturn(new BigDecimal(PRICE), new BigDecimal(PRICE + 1.00));
		when(resultSet.getBigDecimal("XXXXXXX")).thenReturn(new BigDecimal(1), new BigDecimal(2));
		when(resultSet.next()).thenReturn(true, true, false);

		adjuster.correctBy(order());
		
		assertEquals(PRICE, entry1.getTotalPrice(), 0.1);
		assertEquals(PRICE + 1.00, entry2.getTotalPrice(), 0.1);
	}

	@Test
	public void shouldSaveOnlyTheCorrectedItems() throws Exception {
		CartEntryModel entry1 = createCartEntry(1, PRICE, QUANTITY, ARTICLE_NUMBER);
		CartEntryModel entry2 = createCartEntry(2, PRICE + 10.00, QUANTITY, ARTICLE_NUMBER);
		when(resultSet.getBigDecimal("XXXXXXX")).thenReturn(new BigDecimal(PRICE), new BigDecimal(PRICE + 1.00));
		when(resultSet.getBigDecimal("XXXXXXX")).thenReturn(new BigDecimal(1), new BigDecimal(2));
		when(resultSet.next()).thenReturn(true, true, false);

		adjuster.correctBy(order());
		
		assertEquals(PRICE, entry1.getTotalPrice(), 0.1);
		assertEquals(PRICE + 1.00, entry2.getTotalPrice(), 0.1);
		verify(cartHandler, never()).saveCartEntry(entry1);
		verify(cartHandler).saveCartEntry(entry2);
	}

	@Test
	public void shouldCorrectTheQuantityOfAnEntry() throws Exception {
		CartEntryModel entry = createCartEntry(LINE_NUMBER, PRICE, QUANTITY + 1l, ARTICLE_NUMBER);
		when(resultSet.next()).thenReturn(true, false);

		adjuster.correctBy(order());
		
		assertThat(entry.getQuantity(), equalTo(QUANTITY));
	}

	@Test
	public void shouldCorrectTheQuantityOfMoreThanOneEntry() throws Exception {
		CartEntryModel entry1 = createCartEntry(1, PRICE, QUANTITY + 1l, ARTICLE_NUMBER);
		CartEntryModel entry2 = createCartEntry(2, PRICE, QUANTITY + 2l, ARTICLE_NUMBER);
		when(resultSet.getBigDecimal("XXXXXXX")).thenReturn(new BigDecimal(QUANTITY), new BigDecimal(QUANTITY + 10l));
		when(resultSet.getBigDecimal("XXXXXXX")).thenReturn(new BigDecimal(1), new BigDecimal(2));
		when(resultSet.next()).thenReturn(true, true, false);

		adjuster.correctBy(order());
		
		assertThat(entry1.getQuantity(), equalTo(QUANTITY));
		assertThat(entry2.getQuantity(), equalTo(QUANTITY + 10l));
	}

	@Test
	public void shouldOnlySaveTheCorrectedQuantity() throws Exception {
		CartEntryModel entry1 = createCartEntry(1, PRICE, QUANTITY + 1l, ARTICLE_NUMBER);
		CartEntryModel entry2 = createCartEntry(2, PRICE, QUANTITY, ARTICLE_NUMBER);
		when(resultSet.getBigDecimal("XXXXXXX")).thenReturn(new BigDecimal(QUANTITY), new BigDecimal(QUANTITY));
		when(resultSet.getBigDecimal("XXXXXXX")).thenReturn(new BigDecimal(1), new BigDecimal(2));
		when(resultSet.next()).thenReturn(true, true, false);

		adjuster.correctBy(order());
		
		assertThat(entry1.getQuantity(), equalTo(QUANTITY));
		assertThat(entry2.getQuantity(), equalTo(QUANTITY));
		verify(cartHandler).saveCartEntry(entry1);
		verify(cartHandler, never()).saveCartEntry(entry2);
	}

	@Test(expected=RuntimeException.class)
	public void shouldThrowAnExceptionIfAnArticleNumberIsntInTheOrder() throws Exception {
		createCartEntry(LINE_NUMBER, PRICE, QUANTITY, ARTICLE_NUMBER + "wrongSuffix");
		when(resultSet.next()).thenReturn(true, false);

		adjuster.correctBy(order());
	}

	@Test(expected=RuntimeException.class)
	public void shouldThrowAnExceptionIfThereAreMoreItemsInTheCartThanInTheOrder() throws Exception {
		createCartEntry(LINE_NUMBER, PRICE, QUANTITY, ARTICLE_NUMBER);
		createCartEntry(LINE_NUMBER, PRICE, QUANTITY, ARTICLE_NUMBER);
		when(resultSet.next()).thenReturn(true, false);

		adjuster.correctBy(order());
	}

	@Test(expected=RuntimeException.class)
	public void shouldThrowAnExceptionIfThereAreMoreItemsInTheOrderThanInTheCart() throws Exception {
		createCartEntry(LINE_NUMBER, PRICE, QUANTITY, ARTICLE_NUMBER);
		when(resultSet.getBigDecimal("XXXXXXX")).thenReturn(new BigDecimal(QUANTITY), new BigDecimal(QUANTITY));
		when(resultSet.getBigDecimal("XXXXXXX")).thenReturn(new BigDecimal(1), new BigDecimal(2));
		when(resultSet.next()).thenReturn(true, true, false);

		adjuster.correctBy(order());
	}

	public CartEntryModel createCartEntry(int lineNumber, double price, long quantity, String articleNumber) {
		ProductModel product = mock(ProductModel.class);
		when(product.getCode()).thenReturn(articleNumber);
		CartEntryModel entry = new CartEntryModel();
		entry.setTotalPrice(price);
		entry.setEntryNumber(lineNumber);
		entry.setQuantity(quantity);
		entry.setProduct(product);
		cart.getEntries().add(entry);
		return entry;
	}

	public Order order() {
		Order order = Order.create(resultSet);
		return order;
	}

}
