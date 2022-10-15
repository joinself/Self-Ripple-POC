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
    <title>VPay Admin</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
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
      <div><strong>Withdrawals</strong></div>
      <table class="table table-striped table-hover">
        <tr>
          <th>CREATED</th>
          <th>ACCOUNT</th>
          <th>AMOUNT</th>
          <th>CURRENCY</th>
          <th>BANK</th>
          <th>BANK AC</th>
          <th>HOLDER</th>
          <th>STATUS</th>
          <th></th>
        </tr>
        <c:forEach var="withdrawal" items="${withdrawalList}" >
          <c:set var="user" value="${repository.getUserbyId(withdrawal.userid)}"/>
          <tr>
            <td><c:out value="${withdrawal.timecreated}"/></td>
            <td><c:out value="${user.address}"/></td>
            <td><c:out value="${withdrawal.amount}"/></td>
            <td><c:out value="${withdrawal.currency}"/></td>
            <td><c:out value="${withdrawal.bankname}"/></td>
            <td><c:out value="${withdrawal.accountnumber}"/></td>
            <td><c:out value="${withdrawal.accountholder}"/></td>
            <td><c:out value="${withdrawal.status}"/></td>
            <td><a href="/admin/processwithdrawal?hash=<c:out value='${withdrawal.txhash}'/>&action=a">APPROVE</a><br/><a href="/admin/processwithdrawal?hash=<c:out value='${withdrawal.txhash}'/>&action=r">REJECT</a></td>
          </tr>
        </c:forEach>
      </table>

    </div>

  </body>
</html>