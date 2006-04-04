/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are prohibited, unless you have been explicitly granted 
 * more rights by Abacus Research AG.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS 
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND 
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR 
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package temp;

import java.util.ArrayList;
import java.util.List;

public class DynamicRangeComputation
{

	public static void main( String[] args )
	{
		new DynamicRangeComputation().run();
	}


	private List<Employee> employees = new ArrayList<Employee>();
	private double bonusTotal;
	private double overtimeRate;


	private void run()
	{
		setOvertimeRate( 50.0 );
		setBonusTotal( 20000.0 );
		new Employee( "Ann", 5600.0, 20.0 );
		new Employee( "Beth", 5400.0, 15.0 );
		new Employee( "Charlie", 5500.0, 0.0 );
		for (Employee employee : getEmployees()) {
			System.out.println( employee.getBonusAmount() );
		}
	}


	double getOvertimeRate()
	{
		return this.overtimeRate;
	}


	private void setOvertimeRate( double _d )
	{
		this.overtimeRate = _d;
	}


	double getBonusTotal()
	{
		return this.bonusTotal;
	}


	private void setBonusTotal( double _d )
	{
		this.bonusTotal = _d;
	}


	List<Employee> getEmployees()
	{
		return this.employees;
	}


	private boolean haveSalaryTotal = false;
	private double salaryTotal;


	double getSalaryTotal()
	{
		if (!this.haveSalaryTotal) {
			double sum = 0.0;
			for (Employee employee : getEmployees()) {
				sum += employee.getSalary();
			}
			this.salaryTotal = sum;
			this.haveSalaryTotal = true;
		}
		return this.salaryTotal;
	}


	private class Employee
	{

		private final double baseSalary;
		private final double overtimePaid;


		public Employee(String _string, double _baseSalary, double _overtimePaid)
		{
			this.baseSalary = _baseSalary;
			this.overtimePaid = _overtimePaid;
			DynamicRangeComputation.this.getEmployees().add( this );
		}


		double getBonusAmount()
		{
			return getBonusTotal() * getBonusPercentage();
		}


		private double getBonusPercentage()
		{
			return getSalary() / getSalaryTotal();
		}


		private boolean haveSalary = false;
		private double salary;


		double getSalary()
		{
			if (!this.haveSalary) {
				this.salary = getBaseSalary() + getOvertimePaid() * getOvertimeRate();
				this.haveSalary = true;
			}
			return this.salary;
		}


		private double getBaseSalary()
		{
			return this.baseSalary;
		}


		private double getOvertimePaid()
		{
			return this.overtimePaid;
		}


	}
}
