<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"  %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <title>VPay Admin</title>
  </head>
  <body>
<div class="container">
  <div>
    Nav
    <ul>
      <li><a href="/admin/home">Home</a></li>
      <li><a href="/admin/pendingwithdrawals">Pending Withdrawals</a></li>
    </ul>
  </div>
    <div class="main">
      <header><strong>Company Wallet Balances</strong></header>
      <div><strong>Hot Wallet</strong><br/>
        <c:forEach var="hotWalletBalance" items="${hotWalletList}" >
          Currency:<c:out value="${hotWalletBalance.currency}"/> : <c:out value="${hotWalletBalance.balance}"/><br/>
        </c:forEach> 

        XRP: <c:out value="${hotWalletXrp}" /><br/>
      </div>
      <br/>
      <div><strong>Standby Wallet</strong><br/>
        <c:forEach var="standByWalletBalance" items="${standByWalletList}" >
          Currency:<c:out value="${standByWalletBalance.currency}"/> : <c:out value="${standByWalletBalance.balance}"/><br/>
        </c:forEach>

        XRP: <c:out value="${standByWalletXrp}" /><br/>
      </div>
      <br/>
      <div><strong>Cold Wallet</strong><br/>
        Total User Balance BTC : <c:out value="${coldWalletBtcObligation}"/><br/>
        Total User Balance CNY : <c:out value="${coldWalletCnyObligation}"/><br/>
      </div>
      <br/>
      <div><strong>User Info</strong></div>
      <table class="table table-striped table-hover">
        <tr>
          <th>Address</th>
          <th>Ripple Explorer Link</th>
        </tr>
        <c:forEach var="user" items="${userList}" >
          <tr>
            <td><c:out value="${user.address}"/></td>
            <td><a href="<c:out value="${rippleExplorerBaseUrl}"/><c:out value='${user.address}'/>">Ripple Explorer Link</a></td>
          </tr>
        </c:forEach>
      </table>

    </div>
</div>
  </body>
</html>